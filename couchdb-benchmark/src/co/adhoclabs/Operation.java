package co.adhoclabs;
import java.util.List;

/**
 * An operation to be performed by a thread.
 * 
 * @author Michael Parker (michael.g.parker@gmail.com)
 */
public abstract class Operation {
	enum Type {
		CREATE,
		READ,
		UPDATE,
		DELETE
	}
	
	/**
	 * @return the {@link Type} of operation
	 */
	public abstract Type getType();
	
	// TODO: headers?
	
	/**
	 * @return TODO
	 */
	public abstract String getRequestBody();
}
