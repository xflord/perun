package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Consent;

/**
 * This exception is thrown when trying to delete
 * a consent which is not in the database.
 *
 * @author Matej Hakoš <492968@mail.muni.cz>
 */
public class ConsentNotExistsException extends PerunException {
	static final long serialVersionUID = 0;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public ConsentNotExistsException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ConsentNotExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ConsentNotExistsException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with consent
	 * @param consent consent
	 */
	public ConsentNotExistsException(Consent consent) {
		super(consent.toString());

	}
}
