package in.gov.uidai.aadhaaraddressupdate.Requests;

import in.gov.uidai.aadhaaraddressupdate.Models.Users;

public interface FirebaseCallbackForUser {
    void getResponse(String uid, Users user);
    void getError(String string);
}