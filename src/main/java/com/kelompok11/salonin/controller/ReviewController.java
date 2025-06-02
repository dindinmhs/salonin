package com.kelompok11.salonin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.kelompok11.salonin.model.Booking;
import com.kelompok11.salonin.model.Review;
import com.kelompok11.salonin.repository.BookingRepository;
import com.kelompok11.salonin.service.ReviewService;

import org.springframework.ui.Model;

@Controller
@RequestMapping("/reviews")
public class ReviewController {
    @Autowired
    private ReviewService reviewService;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @GetMapping("/create/{bookingId}")
    public String showReviewForm(@PathVariable Long bookingId, Model model) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new RuntimeException("Booking not found"));
            
        if (booking.getStatus() != Booking.Status.SELESAI) {
            return "redirect:/dashboard?error=Cannot review: Booking is not completed";
        }
        
        model.addAttribute("booking", booking);
        model.addAttribute("review", new Review());
        return "reviews/form";
    }
    
    @PostMapping("/create/{bookingId}")
    public String createReview(@PathVariable Long bookingId, @ModelAttribute Review review) {
        try {
            reviewService.createReview(review, bookingId);
            return "redirect:/dashboard?success=Review submitted successfully";
        } catch (RuntimeException e) {
            return "redirect:/dashboard?error=" + e.getMessage();
        }
    }

    @GetMapping("/view/{bookingId}")
    public String viewReview(@PathVariable Long bookingId, Model model) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        Review review = reviewService.findByBookingId(bookingId)
            .orElseThrow(() -> new RuntimeException("Review not found"));
            
        model.addAttribute("booking", booking);
        model.addAttribute("review", review);
        return "reviews/view";
    }
}