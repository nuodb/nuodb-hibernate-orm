/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.orm.test.version.sybase;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import org.junit.Test;

import org.hibernate.Session;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

import org.hibernate.testing.RequiresDialect;
import org.hibernate.testing.TestForIssue;
import org.hibernate.testing.junit4.BaseCoreFunctionalTestCase;

import org.hibernate.dialect.SybaseASEDialect;
import org.hibernate.type.BasicType;
import org.hibernate.type.descriptor.java.PrimitiveByteArrayJavaType;
import org.hibernate.type.descriptor.jdbc.VarbinaryJdbcType;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * @author Gail Badner
 */
@RequiresDialect( SybaseASEDialect.class )
public class SybaseTimestampComparisonAnnotationsTest extends BaseCoreFunctionalTestCase {

	@Test
	@TestForIssue( jiraKey = "HHH-10413" )
	public void testComparableTimestamps() {
        final BasicType<?> versionType = sessionFactory()
				.getMappingMetamodel()
				.getEntityDescriptor(Thing.class.getName()).getVersionType();
		assertTrue( versionType.getJavaTypeDescriptor() instanceof PrimitiveByteArrayJavaType );
		assertTrue( versionType.getJdbcType() instanceof VarbinaryJdbcType );

		Session s = openSession();
		s.getTransaction().begin();
		Thing thing = new Thing();
		thing.name = "n";
		s.persist( thing );
		s.getTransaction().commit();
		s.close();

		byte[] previousVersion = thing.version;
		for ( int i = 0 ; i < 20 ; i++ ) {
			try {
				Thread.sleep(1000);                 //1000 milliseconds is one second.
			} catch(InterruptedException ex) {
				Thread.currentThread().interrupt();
			}

			s = openSession();
			s.getTransaction().begin();
			thing.name = "n" + i;
			thing = (Thing) s.merge( thing );
			s.getTransaction().commit();
			s.close();

			assertTrue( versionType.compare( previousVersion, thing.version ) < 0 );
			previousVersion = thing.version;
		}

		s = openSession();
		s.getTransaction().begin();
		s.delete( thing );
		s.getTransaction().commit();
		s.close();
	}

	@Override
	protected Class[] getAnnotatedClasses() {
		return new Class[] { Thing.class };
	}

	@Entity
	@Table(name="thing")
	public static class Thing {
		@Id
		private long id;

		@Version
		@Generated(GenerationTime.ALWAYS)
		@Column(name = "verNum", columnDefinition = "timestamp") // NUODB 2025-04-11: ver is reserved word
		private byte[] version;

		private String name;

	}

}
