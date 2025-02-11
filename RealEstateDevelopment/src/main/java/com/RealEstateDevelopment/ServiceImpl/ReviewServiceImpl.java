package com.RealEstateDevelopment.ServiceImpl;

import com.RealEstateDevelopment.Entity.Review;
import com.RealEstateDevelopment.Repository.ReviewRepository;
import com.RealEstateDevelopment.Service.ReviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReviewServiceImpl implements ReviewService {

    private static final Logger logger = LoggerFactory.getLogger(ReviewServiceImpl.class);
    @Autowired
    private ReviewRepository reviewRepository;
    @Override
    public List<Review> getAllReviews() {
        logger.info("Fetching all reviews from the database");
        List<Review> reviews = reviewRepository.findAll();
        logger.info("Total reviews fetched: {}", reviews.size());
        return reviews;
    }
@Override
    public Review saveReview(Review review) {
        logger.info("Saving a new review: {}", review);
        Review savedReview = reviewRepository.save(review);
        logger.info("Review saved successfully with ID: {}", savedReview.getId());
        return savedReview;
    }

    @Override
    public Optional<Review> getReviewById(Long id) {
        return reviewRepository.findById(id);
    }

@Override
    public Optional<Review> updateReview(Long id, Review updatedReview) {
        Optional<Review> existingReview = reviewRepository.findById(id);

        if (existingReview.isPresent()) {
            Review review = existingReview.get();
            review.setCleanlinessRating(updatedReview.getCleanlinessRating());
            review.setComment(updatedReview.getComment());
            review.setLocationRating(updatedReview.getLocationRating());
            review.setValueForMoneyRating(updatedReview.getValueForMoneyRating());
            reviewRepository.save(review);
            return Optional.of(review);
        } else {
            return Optional.empty();
        }
    }
@Override
    public boolean deleteReview(Long id) {
        if (reviewRepository.existsById(id)) {
            reviewRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
