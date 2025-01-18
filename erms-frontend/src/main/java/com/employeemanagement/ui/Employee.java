        package com.employeemanagement.ui;

        import com.fasterxml.jackson.annotation.JsonProperty;
        import lombok.Data;

        import java.util.Date;

        @Data
        public class Employee {
                @JsonProperty("empId")
                private Long empId;

                @JsonProperty("firstName")
                private String firstName;

                @JsonProperty("lastName")
                private String lastName;

                @JsonProperty("email")
                private String email;

                @JsonProperty("hireDate")
                private String hireDate;

                @JsonProperty("jobTitle")
                private String jobTitle;

                @JsonProperty("departmentId")
                private Long departmentId;

                @JsonProperty("managerId")
                private Long managerId;

                @JsonProperty("salary")
                private double salary;

                @JsonProperty("status")
                private String status;

                @JsonProperty("departmentName")
                private String departmentName;

                @JsonProperty("managerName")
                private String managerName;

                @JsonProperty("createdAt")
                private String createdAt;

                @JsonProperty("updatedAt")
                private String updatedAt;

                @JsonProperty("createdBy")
                private String createdBy;

                @JsonProperty("updatedBy")
                private String updatedBy;

        }

