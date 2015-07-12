package no.java.moosehead.aggregate;

import no.java.moosehead.MoosheadException;

public class WorkshopCanNotBeAddedException extends MoosheadException{
    WorkshopCanNotBeAddedException(String message, Exception e){
        super(message, e);
    }

    WorkshopCanNotBeAddedException(String message){
        super(message);
    }
}
