/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.orm.test.annotations.lob;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/**
 * @author Gail Badner
 */
@Entity
@Table(name = "ver_lob_book")
public class VersionedBook extends AbstractBook{
	private Integer id;
	private Integer version;
	
	@Id
	@GeneratedValue
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Version
	//@Column(name = "ver")
	@Column(name = "verNum") // NUODB 2025-05-08: ver is reserved word

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer i) {
		version = i;
	}	
}
