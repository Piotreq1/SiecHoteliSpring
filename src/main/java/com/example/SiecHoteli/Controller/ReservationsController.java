package com.example.SiecHoteli.Controller;

import com.example.SiecHoteli.Entity.Reservations;
import com.example.SiecHoteli.Repo.ReservRepository;
import com.example.SiecHoteli.Repo.RoomRepository;
import com.example.SiecHoteli.Repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/reserv")
public class ReservationsController {
    private final ReservRepository reservRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;

    @Autowired
    public ReservationsController(ReservRepository reservRepository, UserRepository userRepository, RoomRepository roomRepository) {
        this.reservRepository = reservRepository;
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
    }

    @GetMapping("/getAll")
    public List<Reservations> getAllReservations() {
        return reservRepository.findAll();
    }


    @PostMapping("/add")
    public ResponseEntity<Object> createReservation(@RequestBody ReservationsRequest reservation) {
        Date today = new Date();
        Date startDate = reservation.getStartDate();
        Date endDate = reservation.getEndDate();

        if (startDate.after(endDate)) {
            return ResponseEntity.badRequest().body("Start date cannot be after end date");
        }

        if (startDate.before(today)) {
            return ResponseEntity.badRequest().body("Start date cannot be before today");
        }

        boolean is_free = roomRepository.getAvailabilityByRoomID(reservation.getRoom().getRoomID());
        if(!is_free){
            return ResponseEntity.badRequest().body("This room is occupied");
        }
    
        boolean is_user = userRepository.existsById(reservation.getUserID());
        if(!is_user){
            return ResponseEntity.badRequest().body("User is not in database");
        }
    
        roomRepository.setAvailabilityByRoomID(reservation.getRoom().getRoomID(),false);
        var reserv = Reservations.builder()
                .hotel(reservation.getHotel())
                .room(reservation.getRoom())
                .userID(reservation.getUserID())
                .startDate(reservation.getStartDate())
                .endDate(reservation.getEndDate())
                .build();

        reservRepository.save(reserv);
        return ResponseEntity.ok("Reservation added successfully");
    }

    @DeleteMapping("/delete/{reservID}")
    public ResponseEntity<String> deleteReserv(@PathVariable Integer reservID) {
        if (reservRepository.existsById(reservID)) {
            reservRepository.deleteById(reservID);
            return ResponseEntity.ok("Hotel deleted successfully");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/update/{reservID}")
    public ResponseEntity<String> updateReserv(@PathVariable("reservID") Integer reservID,
                                              @RequestBody ReservationsRequest request) {

        Optional<Reservations> optional = reservRepository.findById(reservID);
        if (optional.isPresent()) {
            Reservations reserv = optional.get();
            if (request.getHotel() != null) {
                reserv.setHotel(request.getHotel());
            }
            if (request.getRoom() != null) {
                boolean is_free = roomRepository.getAvailabilityByRoomID(request.getRoom().getRoomID());
                if(!is_free){
                    return ResponseEntity.badRequest().body("This room is occupied");
                }

                reserv.setRoom(request.getRoom());
            }
            if (request.getUserID() != null) {
                boolean is_user = userRepository.existsById(request.getUserID());
                if(!is_user){
                    return ResponseEntity.badRequest().body("User is not in database");
                }

                reserv.setUserID(request.getUserID());
            }

            Date today = new Date();
            Date startDate = reserv.getStartDate();
            Date endDate = reserv.getEndDate();
            if (request.getStartDate() != null) {
                startDate = request.getStartDate();
            }
            if (request.getEndDate() != null) {
                endDate = request.getEndDate();
            }

            if (startDate.after(endDate)) {
                return ResponseEntity.badRequest().body("Start date cannot be after end date");
            }
            if (startDate.before(today)) {
                return ResponseEntity.badRequest().body("Start date cannot be before today");
            }

            reserv.setStartDate(startDate);
            reserv.setEndDate(endDate);

            reservRepository.save(reserv);
            return ResponseEntity.ok("Hotel updated successfully");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/deleteExpired")
    public ResponseEntity<String> deleteExpiredReservations() {
        reservRepository.deleteExpiredReservations();
        return ResponseEntity.ok("Expired reservations deleted successfully");
    }
}
