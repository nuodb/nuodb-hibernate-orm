/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.testing.cleaner;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.Driver;
import java.util.Properties;

import javax.swing.JOptionPane;

import org.hibernate.cfg.AvailableSettings;

import com.nuodb.jdbc.DataSource;

/**
//  * NUODB COPY OF HIBERNATE CLASS. Checks the Hibernate config and uses a DataSource
 * instead of the Driver to get a connection.
 * <p>
 * Modified to use a DataSource instead of Driver.connect (which doesn't understand
 * the NuoDB Hibernate JDBC prefix 'com.nuodb.hib').
 * 
 * @author Christian Beikov
 */
public final class JdbcConnectionContext {
	@SuppressWarnings("unused")
	private static final Driver driver;
	private static final String url;
	private static final String user;
	private static final String password;
	private static final Properties properties;

	static {
		final Properties connectionProperties = new Properties();

		try (InputStream inputStream = Thread.currentThread()
				.getContextClassLoader()
				.getResourceAsStream( "hibernate.properties" )) {
			connectionProperties.load( inputStream );

			final String driverClassName = connectionProperties.getProperty(AvailableSettings.DRIVER);

			// NUODB: START Sanity check - is it getting the right properties?
			if (!driverClassName.contains("nuodb"))
				JOptionPane.showMessageDialog(null, "ERROR: Connection Properties are " 
					+ connectionProperties.toString().replaceAll(",", System.lineSeparator()));
			//NUODB: END

			driver = (Driver) Class.forName(driverClassName).newInstance();
			url = connectionProperties.getProperty(AvailableSettings.URL);
			user = connectionProperties.getProperty(AvailableSettings.USER);
			password = connectionProperties.getProperty(AvailableSettings.PASS);
			Properties p = new Properties();
			if (user != null) {
				p.put("user", user);
			}
			if (password != null) {
				p.put("password", password);
			}
			properties = p;

			// the URL so we can use these properties to create a NuoDB datasource.
			properties.put("url", url);
			System.out.println("Done static init for JdbcConnectionContext");
		} catch (Exception e) {
			System.out.println("Exception in JdbcConnectionContext: " + e);
			throw new IllegalArgumentException(e);
		}
	}

	public static void load() {
	}
	
	public static void work(ConnectionConsumer work) {
		System.out.println("JdbcConnectionContext.work");

		// NUODB: driver.connect returns null, use our data source instead.
		try (DataSource ds = new DataSource(properties); Connection connection = ds.getConnection()) { // driver.connect(url, properties)) {
			connection.setAutoCommit(false);
			work.consume(connection);
		} catch (Exception e) {
			throw new IllegalArgumentException("Failed running work", e);
		}
}

	public static <R> R workReturning(ConnectionFunction<R> work) {
//		System.out.println("JdbcConnectionContext.workReturning");
//		System.out.println("driver = " + driver);
//		System.out.println("url = " + url);
//		System.out.println("props = " + properties);

		// NUODB: driver.connect returns null, use our data source instead.
		try (DataSource ds = new DataSource(properties); Connection connection = ds.getConnection()) { // driver.connect(url, properties)) {
			connection.setAutoCommit(false);
			return work.apply(connection);
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	public static interface ConnectionConsumer {
		void consume(Connection c) throws Exception;
	}

	public static interface ConnectionFunction<R> {
		R apply(Connection c) throws Exception;
	}

	public JdbcConnectionContext() {
	}
}