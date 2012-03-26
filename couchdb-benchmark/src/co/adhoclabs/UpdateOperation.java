package co.adhoclabs;
/**
 * An operation that executes the HTTP {@code PUT} method.
 * 
 * @author Michael Parker (michael.g.parker@gmail.com)
 */
public class UpdateOperation extends Operation {
	@Override
	public Type getType() {
		return Type.UPDATE;
	}

	@Override
	public String getRequestBody() {
		// TODO Auto-generated method stub
		return null;
	}
}
