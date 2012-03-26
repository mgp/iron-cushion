package co.adhoclabs;
/**
 * An operation that executes the HTTP {@code DELETE} method.
 * 
 * @author Michael Parker (michael.g.parker@gmail.com)
 */
public class DeleteOperation extends Operation {
	@Override
	public Type getType() {
		return Type.DELETE;
	}

	@Override
	public String getRequestBody() {
		// TODO Auto-generated method stub
		return null;
	}
}
