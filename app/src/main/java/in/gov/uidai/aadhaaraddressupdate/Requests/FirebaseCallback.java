package in.gov.uidai.aadhaaraddressupdate.Requests;

public interface FirebaseCallback {
    void getResponse(String uid);
    void getError(String string);
}
