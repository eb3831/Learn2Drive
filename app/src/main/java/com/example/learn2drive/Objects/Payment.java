package com.example.learn2drive.Objects;

import java.io.Serializable;

/**
 * Represents a payment entity for a driving lesson.
 * This class stores financial details including price, payment method, and current status.
 * Implements Serializable to allow passing payment objects between components.
 */
public class Payment implements Serializable
{
    private int price;
    private  int method;
    private int paymentStatus;

    /**
     * Default constructor initializing payment fields to zero.
     * Required for Firebase data mapping.
     */
    public Payment()
    {
        this.price = 0;
        this.method = 0;
        this.paymentStatus = 0;
    }

    /**
     * Parameterized constructor to create a full payment object.
     * @param price The cost of the lesson.
     * @param method The selected payment method (e.g., Cash, Credit Card).
     * @param paymentStatus The current state of the transaction (e.g., Paid, Pending).
     */
    public Payment(int price, int method, int paymentStatus)
    {
        this.price = price;
        this.method = method;
        this.paymentStatus = paymentStatus;
    }

    public void setPrice(int price)
    {
        this.price = price;
    }

    public int getPrice()
    {
        return price;
    }

    public void setMethod(int method)
    {
        this.method = method;
    }

    public int getMethod()
    {
        return method;
    }

    public void setPaymentStatus(int paymentStatus)
    {
        this.paymentStatus = paymentStatus;
    }

    public int getPaymentStatus()
    {
        return paymentStatus;
    }
}