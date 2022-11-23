package de.elbosso.tools.rfc3161timestampingserver.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "rfc3161timestamp"
/*	,indexes = {
		@Index(name= "rfc3161timestamp_pkey", columnList="id")
	}*/
)
public class Rfc3161timestamp
{

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@NotNull
	@Column(name = "id"
		, nullable = false
		, updatable = false
		, length=19
		, precision=19
		, scale=2
	)
	private java.math.BigDecimal m_rfc3161timestamp_id;

	@Column(name = "creation_date"
		, length=29
		, precision=29
		, scale=6
	)
	private java.sql.Timestamp m_rfc3161timestamp_creation_date;

	@NotNull
	@Column(name = "message_imprint_alg_oid"
		, nullable = false
		, length=255
		, precision=255
		, scale=0
	)
	private String m_rfc3161timestamp_message_imprint_alg_oid;

	@Column(name = "message_imprint_digest_base64"
		, length=1024
		, precision=1024
		, scale=0
	)
	private String m_rfc3161timestamp_message_imprint_digest_base64;

	@Column(name = "tsr_data"
	)
	@Lob
	private byte[] m_rfc3161timestamp_tsr_data;

	@Column(name = "message_imprint_digest_hex"
		, length=1024
		, precision=1024
		, scale=0
	)
	private String m_rfc3161timestamp_message_imprint_digest_hex;

//id
	public java.math.BigDecimal getId()
	{
		return m_rfc3161timestamp_id;
	}
	public void setId(java.math.BigDecimal v_id)
	{
		m_rfc3161timestamp_id=v_id;
	}
//creation_date
	public java.sql.Timestamp getCreation_date()
	{
		return m_rfc3161timestamp_creation_date;
	}
	public void setCreation_date(java.sql.Timestamp v_creation_date)
	{
		m_rfc3161timestamp_creation_date=v_creation_date;
	}
//message_imprint_alg_oid
	public String getMessage_imprint_alg_oid()
	{
		return m_rfc3161timestamp_message_imprint_alg_oid;
	}
	public void setMessage_imprint_alg_oid(String v_message_imprint_alg_oid)
	{
		m_rfc3161timestamp_message_imprint_alg_oid=v_message_imprint_alg_oid;
	}
//message_imprint_digest_base64
	public String getMessage_imprint_digest_base64()
	{
		return m_rfc3161timestamp_message_imprint_digest_base64;
	}
	public void setMessage_imprint_digest_base64(String v_message_imprint_digest_base64)
	{
		m_rfc3161timestamp_message_imprint_digest_base64=v_message_imprint_digest_base64;
	}
//tsr_data
	public byte[] getTsr_data()
	{
		return m_rfc3161timestamp_tsr_data;
	}
	public void setTsr_data(byte[] v_tsr_data)
	{
		m_rfc3161timestamp_tsr_data=v_tsr_data;
	}
//message_imprint_digest_hex
	public String getMessage_imprint_digest_hex()
	{
		return m_rfc3161timestamp_message_imprint_digest_hex;
	}
	public void setMessage_imprint_digest_hex(String v_message_imprint_digest_hex)
	{
		m_rfc3161timestamp_message_imprint_digest_hex=v_message_imprint_digest_hex;
	}

}