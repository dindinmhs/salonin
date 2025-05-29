package com.kelompok11.salonin.service;

import com.kelompok11.salonin.model.TopupHistory;
import com.kelompok11.salonin.model.User;
import com.kelompok11.salonin.repository.TopupHistoryRepository;
import com.kelompok11.salonin.repository.UserRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TopUpService {

    @Autowired
    private TopupHistoryRepository topupHistoryRepository;
    
    @Autowired
    private UserRepository userRepository;

    public TopupHistory saveTopupHistory(TopupHistory history) {
        return topupHistoryRepository.save(history);
    }

    public void processSuccessfulPayment(String orderId) {
        TopupHistory history = topupHistoryRepository.findByMidtransId(orderId);
        if (history != null) {
            history.setStatus(TopupHistory.Status.SUKSES);
            
            // Update user balance
            User user = history.getUser();
            user.setBalance(user.getBalance() + history.getAmount());
            
            userRepository.save(user);
            topupHistoryRepository.save(history);
        }
    }

    public List<TopupHistory> getTopupHistoryByUser(User user) {
        return topupHistoryRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
}