package uk.ac.ebi.fgpt.zooma.search;

class ZoomaException extends Exception
{
    //Parameterless Constructor
    public ZoomaException() {}

    //Constructor that accepts a message
    public ZoomaException(String message)
    {
        super(message);
    }
}
