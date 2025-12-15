package com.example.learn2drive.Objects;

public class Payment
{
    private int price;
    private  int method;
    private int paymentStatus;

    public Payment()
    {
        this.price = 0;
        this.method = 0;
        this.paymentStatus = 0;
    }

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
