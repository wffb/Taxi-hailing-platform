package DAO.uow;

import config.DBconfig;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * combine connection pool with UoW
 * responsibility:
 */
public class UoW implements AutoCloseable {

    // ========== static part: connection and pool management ==========

    private static final ThreadLocal<Connection> connectionHolder = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> transactionActive = ThreadLocal.withInitial(() -> false);

    private static volatile ThreadPoolExecutor executor;
    private static final Object EXECUTOR_LOCK = new Object();

    private static final String DB_URL = DBconfig.DB_URL;
    private static final String DB_USER = DBconfig.DB_USER;
    private static final String DB_PASSWORD = DBconfig.DB_PASSWORD;

    // 统计
    private static final AtomicInteger activeConnections = new AtomicInteger(0);
    private static final AtomicInteger totalConnectionsCreated = new AtomicInteger(0);

    static {
        initializeExecutor();
    }

    private static void initializeExecutor() {
        executor = new ThreadPoolExecutor(
                5, 20, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100),
                new ThreadFactory() {
                    private final AtomicInteger counter = new AtomicInteger(1);
                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "UoW-Worker-" + counter.getAndIncrement());
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    // ========== instance part : single operation ==========

    private final List<DatabaseOperation> operations;
    private boolean isCommitted = false;

    public UoW() {
        this.operations = new ArrayList<>();
    }

    /**
     * Execute a transactional task
     */
    public void execute(TransactionalTask task) throws SQLException {
        beginTransaction();
        try {
            task.execute(this);
            commit();
        } catch (Exception e) {
            rollback();
            throw new SQLException("transaction execute failed", e);
        }
    }

    /**
     * Async completion of a transactional task
     */
    public static Future<Boolean> executeAsync(TransactionalTask task) {

        return executor.submit(() -> {

            try (UoW uow = new UoW()) {
                uow.execute(task);

            } catch (SQLException e) {

                System.err.println("transaction execute failed: " + e.getMessage());
                return false;

            } finally {
                releaseThreadLocalConnection();
            }

            return true;
        });
    }

    public static List<Future<Boolean>> executeAsyncBatch(List<TransactionalTask> tasks) {
        List<Future<Boolean>> futures = new ArrayList<>();
        for (TransactionalTask task : tasks) {
            futures.add(executeAsync(task));
        }
        return futures;
    }

    /**
     * wait for all futures to complete
     */
    public static void awaitAll(List<Future<Void>> futures) throws InterruptedException, ExecutionException {
        for (Future<Void> future : futures) {
            future.get();
        }
    }



    // ========== transaction management ==========

    private void beginTransaction() throws SQLException {
        if (transactionActive.get()) {
            throw new IllegalStateException("transaction already active");
        }

        Connection conn = getConnection();
        conn.setAutoCommit(false);
        transactionActive.set(true);

        System.out.println("✓ [" + Thread.currentThread().getName() + "] transaction begin");
    }

    private void commit() throws SQLException {
        if (!transactionActive.get()) {
            throw new IllegalStateException("no active transaction");
        }

        try {
            // execute all operations
            executeOperations();

            // submit db operations to thread pool
            Connection conn = connectionHolder.get();
            if (conn != null) {
                conn.commit();
                System.out.println("✓ [" + Thread.currentThread().getName() +
                        "] transaction commit,executed " + operations.size() + " operations");
            }

            isCommitted = true;

        } catch (SQLException e) {
            rollback();
            throw e;
        } finally {
            operations.clear();
            transactionActive.set(false);
        }
    }

    private void rollback() {
        Connection conn = connectionHolder.get();
        if (conn != null) {
            try {
                conn.rollback();
                System.out.println("✗ [" + Thread.currentThread().getName() + "] rollback");
            } catch (SQLException e) {
                System.err.println("rollback failed: " + e.getMessage());
            }
        }
        operations.clear();
        transactionActive.set(false);
    }



    // ========== connection management ==========

    private Connection getConnection() throws SQLException {
        Connection conn = connectionHolder.get();

        if (conn == null || conn.isClosed()) {
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            conn.setAutoCommit(false);
            connectionHolder.set(conn);
            activeConnections.incrementAndGet();
            totalConnectionsCreated.incrementAndGet();

            System.out.println("✓ [" + Thread.currentThread().getName() + "] created new connection");
        }

        return conn;
    }

    private static void releaseThreadLocalConnection() {
        Connection conn = connectionHolder.get();
        if (conn != null) {
            try {
                if (transactionActive.get()) {
                    conn.rollback();
                }
                conn.close();
                activeConnections.decrementAndGet();
                System.out.println("✓ [" + Thread.currentThread().getName() + "] connection released");
            } catch (SQLException e) {
                System.err.println("close connection failed: " + e.getMessage());
            } finally {
                connectionHolder.remove();
                transactionActive.remove();
            }
        }
    }


    // ========== db operaions management ==========

    private void executeOperations() throws SQLException {
        Connection conn = getConnection();
        for (DatabaseOperation operation : operations) {
            operation.execute(conn);
        }
    }

    public void registerInsert(String sql, Consumer<PreparedStatement> parameterSetter) {
        operations.add(new InsertOperation(sql, parameterSetter));
    }

    public void registerUpdate(String sql, Consumer<PreparedStatement> parameterSetter) {
        operations.add(new UpdateOperation(sql, parameterSetter));
    }

    public void registerDelete(String sql, Consumer<PreparedStatement> parameterSetter) {
        operations.add(new DeleteOperation(sql, parameterSetter));
    }

    /**
     * query the database and return a result set (no need to commit)
     */
    public <T> T query(String sql, Consumer<PreparedStatement> parameterSetter,
                       ResultSetExtractor<T> extractor) throws SQLException {
        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (parameterSetter != null) {
                parameterSetter.accept(stmt);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                return extractor.extract(rs);
            }
        }
    }

    // ========== 统计和管理 ==========

    public static String getStats() {
        return String.format(
                "UoW status - thread pool: %d/%d active, queue: %d, completed tasks: %d | connection: %d active, %d created",
                executor.getActiveCount(),
                executor.getPoolSize(),
                executor.getQueue().size(),
                executor.getCompletedTaskCount(),
                activeConnections.get(),
                totalConnectionsCreated.get()
        );
    }

    public static void shutdown() {
        System.out.println("closing UnitOfWork...");
        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        releaseThreadLocalConnection();
        System.out.println("UnitOfWork closed");
    }

    @Override
    public void close() {
        if (!isCommitted && transactionActive.get()) {
            rollback();
        }
    }

    // ========== internal class ==========

    @FunctionalInterface
    public interface TransactionalTask {
        void execute(UoW uow) throws Exception;
    }

    @FunctionalInterface
    public interface ResultSetExtractor<T> {
        T extract(ResultSet rs) throws SQLException;
    }

    private interface DatabaseOperation {
        void execute(Connection conn) throws SQLException;
    }

    private static class InsertOperation implements DatabaseOperation {
        private final String sql;
        private final Consumer<PreparedStatement> parameterSetter;

        public InsertOperation(String sql, Consumer<PreparedStatement> parameterSetter) {
            this.sql = sql;
            this.parameterSetter = parameterSetter;
        }

        @Override
        public void execute(Connection conn) throws SQLException {
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                parameterSetter.accept(stmt);
                int rows = stmt.executeUpdate();
                System.out.println("  INSERT: " + rows + " 行");
            }
        }
    }

    private static class UpdateOperation implements DatabaseOperation {
        private final String sql;
        private final Consumer<PreparedStatement> parameterSetter;

        public UpdateOperation(String sql, Consumer<PreparedStatement> parameterSetter) {
            this.sql = sql;
            this.parameterSetter = parameterSetter;
        }

        @Override
        public void execute(Connection conn) throws SQLException {
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                parameterSetter.accept(stmt);
                int rows = stmt.executeUpdate();
                System.out.println("  UPDATE: " + rows + " row");
            }
        }
    }

    private static class DeleteOperation implements DatabaseOperation {
        private final String sql;
        private final Consumer<PreparedStatement> parameterSetter;

        public DeleteOperation(String sql, Consumer<PreparedStatement> parameterSetter) {
            this.sql = sql;
            this.parameterSetter = parameterSetter;
        }

        @Override
        public void execute(Connection conn) throws SQLException {
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                parameterSetter.accept(stmt);
                int rows = stmt.executeUpdate();
                System.out.println("  DELETE: " + rows + " row");
            }
        }
    }
}