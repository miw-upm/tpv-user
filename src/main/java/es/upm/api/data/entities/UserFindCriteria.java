package es.upm.api.data.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserFindCriteria {
    private String mobile;
    private String firstName;
    private String familyName;
    private String email;
    private String dni;
    private boolean projection = false;

    public boolean all() {
        return mobile == null && firstName == null && familyName == null && email == null && dni == null;
    }
}
