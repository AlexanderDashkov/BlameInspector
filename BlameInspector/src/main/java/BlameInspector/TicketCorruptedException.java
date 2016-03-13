package BlameInspector;

/**
 * Created by Alexander on 03.03.2016.
 */
public class TicketCorruptedException extends Throwable {

    private String message;

    public TicketCorruptedException(final String message) {
        this.message = message;
    }

    @Override
    public String getMessage(){
        return message;
    }
}
