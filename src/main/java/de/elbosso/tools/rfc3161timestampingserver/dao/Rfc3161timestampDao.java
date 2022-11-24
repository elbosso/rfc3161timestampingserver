package de.elbosso.tools.rfc3161timestampingserver.dao;

import de.elbosso.tools.rfc3161timestampingserver.domain.Rfc3161timestamp;
import de.elbosso.tools.rfc3161timestampingserver.util.JpaDao;

import javax.persistence.NoResultException;
import java.math.BigInteger;
import java.util.Optional;

public class Rfc3161timestampDao extends JpaDao<Rfc3161timestamp>
{
	Rfc3161timestampDao()
	{
		super(Rfc3161timestamp.class);
	}

	public Optional<Rfc3161timestamp> findYoungestByMsgImprintBase64(Rfc3161timestamp timestamp) {
		try {


			return Optional.of(entityManager.createQuery("SELECT r FROM Rfc3161timestamp r WHERE r.m_rfc3161timestamp_message_imprint_digest_base64 = :Imprint ORDER BY r.m_rfc3161timestamp_creation_date DESC", Rfc3161timestamp.class)
					.setParameter("Imprint", timestamp.getMessage_imprint_digest_base64())
					.setMaxResults(1)
					.getResultList().get(0));
		} catch (NoResultException|IndexOutOfBoundsException e) {
			return Optional.empty();
		}
	}
	public Optional<Rfc3161timestamp> findYoungestByMsgDigestAndImprintBase64(Rfc3161timestamp timestamp) {
		try {


			return Optional.of(entityManager.createQuery("SELECT r FROM Rfc3161timestamp r WHERE r.m_rfc3161timestamp_message_imprint_alg_oid = :alg_oid AND r.m_rfc3161timestamp_message_imprint_digest_base64 = :Imprint ORDER BY r.m_rfc3161timestamp_creation_date DESC", Rfc3161timestamp.class)
					.setParameter("alg_oid", timestamp.getMessage_imprint_alg_oid())
					.setParameter("Imprint", timestamp.getMessage_imprint_digest_base64())
					.setMaxResults(1)
					.getResultList().get(0));
		} catch (NoResultException|IndexOutOfBoundsException e) {
			return Optional.empty();
		}
	}
	public Optional<Rfc3161timestamp> findYoungestByMsgImprintHex(Rfc3161timestamp timestamp) {
		try {


			return Optional.of(entityManager.createQuery("SELECT r FROM Rfc3161timestamp r WHERE r.m_rfc3161timestamp_message_imprint_digest_hex = :Imprint ORDER BY r.m_rfc3161timestamp_creation_date DESC", Rfc3161timestamp.class)
					.setParameter("Imprint", timestamp.getMessage_imprint_digest_hex())
					.setMaxResults(1)
					.getResultList().get(0));
		} catch (NoResultException|IndexOutOfBoundsException e) {
			return Optional.empty();
		}
	}
	public Optional<Rfc3161timestamp> findYoungest() {
		try {


			return Optional.of(entityManager.createQuery("SELECT r FROM Rfc3161timestamp r ORDER BY r.m_rfc3161timestamp_creation_date DESC", Rfc3161timestamp.class)
					.setMaxResults(1)
					.getResultList().get(0));
		} catch (NoResultException|IndexOutOfBoundsException e) {
			return Optional.empty();
		}
	}
	public Optional<BigInteger> findTotalNumber() {
		try {


			return Optional.of(entityManager.createQuery("SELECT count(r) FROM Rfc3161timestamp r", BigInteger.class)
					.getSingleResult());
		} catch (NoResultException e) {
			return Optional.empty();
		}
	}
}
