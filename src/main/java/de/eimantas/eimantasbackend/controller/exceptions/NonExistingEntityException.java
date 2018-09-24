package de.eimantas.eimantasbackend.controller.exceptions;

public class NonExistingEntityException extends Exception {

    public NonExistingEntityException(String message) {
        super(message);
    }

}
