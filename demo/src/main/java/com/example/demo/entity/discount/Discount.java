    package com.example.demo.entity.discount;

    import java.time.LocalDateTime;

    import jakarta.persistence.Column;
    import jakarta.persistence.Entity;
    import jakarta.persistence.GeneratedValue;
    import jakarta.persistence.GenerationType;
    import jakarta.persistence.Id;
    import jakarta.persistence.Table;
    import lombok.AllArgsConstructor;
    import lombok.Data;
    import lombok.NoArgsConstructor;

    @Entity
    @Table(name = "discounts")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class Discount {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private int id;

        @Column(name = "code")
        private String code;

        @Column(name = "discount_percentage")
        private double discountPercentage;

        @Column(name = "max_discount_amount")
        private double maxDiscountAmount;

        @Column(name = "start_date")
        private LocalDateTime startDate;

        @Column(name = "end_date")
        private LocalDateTime endDate;

        @Column(name = "is_active")
        private boolean isActive =true;

        @Column(name = "used")
        private boolean used = false;
    }
