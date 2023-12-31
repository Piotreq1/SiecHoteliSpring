package com.example.SiecHoteli.Repo;

import com.example.SiecHoteli.Entity.Hotel;
import com.example.SiecHoteli.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;

@EnableJpaRepositories
@Repository
public interface HotelRepository extends JpaRepository<Hotel, Integer> {
}
