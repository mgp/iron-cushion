package co.adhoclabs;
/**
 * An operation that executes the HTTP {@code POST} method.
 * 
 * @author Michael Parker (michael.g.parker@gmail.com)
 */
public final class CreateOperation extends Operation {
	@Override
	public Type getType() {
		return Type.CREATE;
	}
	
	@Override
	public String getRequestBody() {
		// TODO Auto-generated method stub
		return null;
	}
}
