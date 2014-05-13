package no.java.moosehead.aggregate;

public class WorkshopCanNotBeAddedException extends RuntimeException{
    WorkshopCanNotBeAddedException(String message){
        super(message);
    }
}
