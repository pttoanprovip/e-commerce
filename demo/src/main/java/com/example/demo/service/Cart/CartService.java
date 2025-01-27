package com.example.demo.service.Cart;

import com.example.demo.dto.req.Cart.CartRequest;
import com.example.demo.dto.res.Cart.CartResponse;

public interface CartService {
    CartResponse addItem(CartRequest cartRequest);

    CartResponse removeItem(CartRequest cartRequest);

    CartResponse getCart(int id);
}
