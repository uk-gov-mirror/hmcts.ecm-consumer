package uk.gov.hmcts.reform.ethos.ecm.consumer.domain;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "multipleerrors")
public class MultipleErrors {

    @Id
    protected String multipleref;
    protected String ethoscaseref;
    protected String description;

    public String toString() {
        return "Multiple Reference: '" + this.multipleref
            + "', Ethos Case Reference: '" + this.ethoscaseref
            + "', Description: '" + this.description + "'";
    }

}
