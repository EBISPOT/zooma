package uk.ac.ebi.pride.utilities.ols.web.service.model;

/**
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * Creation date 05/03/2016
 */
public class Identifier {

    public enum IdentifierType{

        IRI("IRI"),
        OWL("OWL"),
        OBO("OBO");

        private String type;

        IdentifierType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    private String identifier;

    private IdentifierType type;

    public Identifier(String identifier, IdentifierType type) {
        this.identifier = identifier;
        this.type = type;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public IdentifierType getType() {
        return type;
    }

    public void setType(IdentifierType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Identifier)) return false;

        Identifier that = (Identifier) o;

        return !(identifier != null ? !identifier.equals(that.identifier) : that.identifier != null) && type == that.type;

    }

    @Override
    public int hashCode() {
        int result = identifier != null ? identifier.hashCode() : 0;
        result = 31 * result + type.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Identifier{" +
                "identifier='" + identifier + '\'' +
                '}';
    }
}
