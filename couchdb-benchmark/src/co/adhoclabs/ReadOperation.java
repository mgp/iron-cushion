package co.adhoclabs;
/**
 * An operation that executes the HTTP {@code GET} method.
 * 
 * @author Michael Parker (michael.g.parker@gmail.com)
 */
public class ReadOperation extends Operation {
	@Override
	public Type getType() {
		return Type.READ;
	}

	@Override
	public String getRequestBody() {
		// TODO Auto-generated method stub
		return null;
	}
}
