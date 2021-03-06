package org.h819.web.jdbc;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.*;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Description : TODO(apache dbutils 工具类)
 * User: h819
 * Date: 2018/2/26
 * ---
 * QueryRunner 类中的每个方法执行过程：
 * 获取 Connection、 Statement
 * 执行 sql
 * 关闭 Connection、 Statement
 * -
 * 所以每次直接用  QueryRunner 中的方法就可以，不必获取和关闭资源
 * 每次执行 sql 语句，QueryRunner 会自动获取和关闭资源一次
 * 就像使用 spring JdbcTemplate 一样
 * ---
 * 如果用到分页，参考 jdbcTemplate 工具类，改写 getList 方法
 * ---
 * 如果是多数据源，重新建立一个 MyDbUtils2 工具类，只是更换数据源
 * 不在尝试统一为一个工具类，代码难看且不方便使用
 */

public class MyDbUtils {

    private static final QueryRunner queryRunner;

    /**
     * 必须使用数据源
     * 如果没有数据源，可以使用 QueryRunner 带 Connection 参数的方法
     * -
     * 下面几个 private 方法不对外提供，只提供直接的执行 sql 语句的方法
     */

    /**
     * 数据源写死，根据情况自己修改
     *
     * @return
     */
    static {
        //静态初始化，系统中之后保留一个 QueryRunner 实例
        queryRunner = new QueryRunner(MyDataSourceFactory.getDBCP2DataSource());
    }


    /**
     * 测试是否联通数据库
     *
     * @param i 重试等待时间
     * @return
     */
    public static boolean isValid(int i) {

        try {
            return queryRunner.getDataSource().getConnection().isValid(i);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 开启事务
     */
    private static void beginTransaction() {
        try {
            // 开启事务
            queryRunner.getDataSource().getConnection().setAutoCommit(false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 回滚事务
     */
    private static void rollback() {

        try {
            queryRunner.getDataSource().getConnection().rollback();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    /**
     * 提交事务
     */
    private static void commit() {
        try {
            queryRunner.getDataSource().getConnection().commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 批量操作，包括批量保存、修改、删除
     *
     * @param sql
     * @param params
     * @return
     */
    public static int[] batch(String sql, Object[][] params) {
        try {
            return queryRunner.batch(sql, params);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return new int[0];
    }


    /**
     * ArrayHandler：把结果集中的第一行数据转成对象数组。
     * ArrayListHandler：把结果集中的每一行数据都转成一个数组，再存放到List中。
     * BeanHandler：将结果集中的第一行数据封装到一个对应的JavaBean实例中。
     * BeanListHandler：将结果集中的每一行数据都封装到一个对应的JavaBean实例中，存放到List里。
     * ColumnListHandler：将结果集中某一列的数据存放到List中。
     * KeyedHandler(name)：将结果集中的每一行数据都封装到一个Map里，再把这些map再存到一个map里，其key为指定的key。
     * MapHandler：将结果集中的第一行数据封装到一个Map里，key是列名，value就是对应的值。
     * MapListHandler：将结果集中的每一行数据都封装到一个Map里，然后再存放到List
     */

    public static int delete(String sql) {

        try {
            return queryRunner.update(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * 删除操作
     *
     * @param sql
     * @param params
     * @return
     */
    public static int delete(String sql, Object... params) {
        try {
            return queryRunner.update(sql, params);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    /**
     * 更新操作
     *
     * @param sql
     * @param params
     * @return
     */
    public static int update(String sql, Object... params) {
        try {
            return queryRunner.update(sql, params);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    public static int update(String sqlUpdate) {
        try {
            return queryRunner.update(sqlUpdate);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    /**
     * 保存操作
     *
     * @param sql
     * @param params
     * @return
     */
    public static int save(String sql, Object... params) {
        try {
            return queryRunner.update(sql, params);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    public static int save(String sql) {
        try {
            return queryRunner.update(sql);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    /**
     * 根据sql查询list对象
     *
     * @param <T>
     * @param sql
     * @param type
     * @param params
     * @return
     */
    public static <T> List<T> getListBean(String sql, Class<T> type, Object... params) {
        try {
            return queryRunner.query(sql, new BeanListHandler<T>(type), params);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return Collections.EMPTY_LIST;
    }

    /**
     * 根据sql查询list对象
     *
     * @param <T>
     * @param sql
     * @param type
     * @return
     */
    public static <T> List<T> getListBean(String sql, Class<T> type) {
        try {
            // BeanListHandler 将ResultSet转换为List<JavaBean>的ResultSetHandler实现类
            return queryRunner.query(sql, new BeanListHandler<T>(type));
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return Collections.EMPTY_LIST;
    }

    /**
     * 根据sql和对象，查询结果并以对象形式返回
     *
     * @param <T>
     * @param sql
     * @param type
     * @param params
     * @return
     */
    public static <T> T getBean(String sql, Class<T> type, Object... params) {
        try {
            return queryRunner.query(sql, new BeanHandler<T>(type), params);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 根据sql和对象，查询结果并以对象形式返回
     *
     * @param <T>
     * @param sql
     * @param type
     * @return
     */
    public static <T> T getBean(String sql, Class<T> type) {
        try {
            // BeanHandler 将ResultSet行转换为一个JavaBean的ResultSetHandler实现类
            return queryRunner.query(sql, new BeanHandler<T>(type));
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 根据传入的sql查询所有记录，以List Map形式返回
     *
     * @param sql
     * @param params
     * @return
     */
    public static List<Map<String, Object>> getListMap(String sql, Object... params) {
        try {
            return queryRunner.query(sql, new MapListHandler(), params);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return Collections.EMPTY_LIST;
    }

    /**
     * 根据传入的sql查询所有记录，以List Map形式返回
     *
     * @param sql
     * @return
     */
    public static List<Map<String, Object>> getListMap(String sql) {
        try {
            // MapListHandler 将ResultSet转换为List<Map>的ResultSetHandler实现类
            return queryRunner.query(sql, new MapListHandler());
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return Collections.EMPTY_LIST;
    }

    /**
     * 根据传入的sql，查询记录，以Map形式返回第一行记录。 注意：如果有多行记录，只会返回第一行，所以适用场景需要注意，可以使用根据主键来查询的场景
     *
     * @param sql
     * @param params
     * @return
     */
    public static Map<String, Object> getFirstRowMap(String sql, Object... params) {
        try {
            return queryRunner.query(sql, new MapHandler(), params);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return Collections.EMPTY_MAP;
    }

    /**
     * 根据传入的sql，查询记录，以Map形式返回第一行记录。 注意：如果有多行记录，只会返回第一行，所以适用场景需要注意，可以使用根据主键来查询的场景
     *
     * @param sql
     * @return
     */
    public static Map<String, Object> getFirstRowMap(String sql) {
        try {
            // MapHandler 将ResultSet的首行转换为一个Map的ResultSetHandler实现类
            return queryRunner.query(sql, new MapHandler());
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return Collections.EMPTY_MAP;
    }

    /**
     * 得到查询记录的条数
     *
     * @param sql
     * @param params
     * @return
     */
    public static int getCount(String sql, Object... params) {
        try {
            Object value = queryRunner.query(sql, new ScalarHandler(), params);
            return objectToInteger(value);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    /**
     * 得到查询记录的条数
     *
     * @param sql
     * @return
     */
    public static int getCount(String sql) {
        try {
            Object value = queryRunner.query(sql, new ScalarHandler());
            return objectToInteger(value);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    private static int objectToInteger(Object obj) {

        try {
            if (obj != null && !obj.toString().trim().equals(""))
                return Integer.parseInt(obj.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        }
        return 0;
    }

    /**
     * 事物执行演示
     */
    private void transactionExample() {

        //下面代码执行了一个事务
        // 如果执行成功，就代表一个完整的事务执行成功了
        // 如果不成功，会自动回滚

        try {
            MyDbUtils.beginTransaction();
            //...  do somethiong
            MyDbUtils.commit();
        } catch (Exception e) {
            e.printStackTrace();
            MyDbUtils.rollback();
        }
    }
}
