package no.java.moosehead.aggregate;

public class WorkshopCanNotBeAddedException extends Exception{
    WorkshopCanNotBeAddedException(String message){
        super(message);
    }
}
