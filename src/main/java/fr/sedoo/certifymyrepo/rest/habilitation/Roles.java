package fr.sedoo.certifymyrepo.rest.habilitation;

/**
 * 
 * 3 JWT authorities
 * 3 functional roles linked to 1 JWT authorities
 *
 */
public class Roles {
	
	/**
	 * Complete administration of the service.
	 * SEDOO members.
	 */
	public final static String AUTHORITY_SUPER_ADMIN = "ROLE_SUPER_ADMIN";
	
	/**
	 * Can view all repositories and reports (read only) and global dashboard
	 * Co-pilote COSO
	 */
	public final static String AUTHORITY_ADMIN = "ROLE_ADMIN";
	
	/**
	 * Role for the management of some repositories
	 * This JWT authority is used for the following functional roles:
	 * <li>EDITOR</li>
	 * <li>CONTRIBUTOR</li>
	 * <li>READER</li>
	 */
	public final static String AUTHORITY_USER = "ROLE_USER";
	
	/**
	 * An editor can create/edit/delete their repositories and their reports
	 */
	public final static String EDITOR = "EDITOR";
	
	/**
	 * A contributor can view their repositories and they can edit related reports
	 */
	public final static String CONTRIBUTOR = "CONTRIBUTOR";
	
	/**
	 * A reader can only read their repositories and the related reports
	 */
	public final static String READER = "READER";

}
