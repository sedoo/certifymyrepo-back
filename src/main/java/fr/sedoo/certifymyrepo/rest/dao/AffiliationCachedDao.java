package fr.sedoo.certifymyrepo.rest.dao;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import fr.sedoo.certifymyrepo.rest.domain.Affiliation;

public class AffiliationCachedDao implements AffiliationDao {

	private AffiliationDao proxyDao;

	LoadingCache<String, Affiliation> cache = CacheBuilder.newBuilder().maximumSize(100)
			.expireAfterWrite(10, TimeUnit.MINUTES).build(new CacheLoader<String, Affiliation>() {
				@Override
				public Affiliation load(String id) {
					return proxyDao.findById(id);
				}
			});

	public AffiliationCachedDao(AffiliationDao proxyDao) {
		this.proxyDao = proxyDao;
	}

	@Override
	public Affiliation save(Affiliation affiliation) {
		cache.invalidate(affiliation.getId());
		return proxyDao.save(affiliation);

	}

	@Override
	public List<Affiliation> findAll() {
		return proxyDao.findAll();
	}

	@Override
	public Affiliation findById(String id) {
		try {
			return cache.get(id);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public void deleteById(String id) {
		cache.invalidate(id);
		proxyDao.deleteById(id);
	}

}
