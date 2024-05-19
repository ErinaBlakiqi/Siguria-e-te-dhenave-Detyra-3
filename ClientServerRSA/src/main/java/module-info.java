module com.example.clientserverrsa {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.clientserverrsa to javafx.fxml;
    exports com.example.clientserverrsa;
}