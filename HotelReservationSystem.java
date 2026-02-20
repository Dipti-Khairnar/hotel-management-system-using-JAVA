import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

// Room Class
class Room implements Serializable {
    private static final long serialVersionUID = 1L;
    private int roomNumber;
    private RoomCategory category;
    private double price;
    private boolean isAvailable;
    
    public enum RoomCategory {
        STANDARD("Standard", 100.0),
        DELUXE("Deluxe", 200.0),
        SUITE("Suite", 350.0);
        
        private String displayName;
        private double basePrice;
        
        RoomCategory(String displayName, double basePrice) {
            this.displayName = displayName;
            this.basePrice = basePrice;
        }
        
        public String getDisplayName() { return displayName; }
        public double getBasePrice() { return basePrice; }
    }
    
    public Room(int roomNumber, RoomCategory category) {
        this.roomNumber = roomNumber;
        this.category = category;
        this.price = category.getBasePrice();
        this.isAvailable = true;
    }
    
    // Getters and Setters
    public int getRoomNumber() { return roomNumber; }
    public RoomCategory getCategory() { return category; }
    public double getPrice() { return price; }
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }
    
    @Override
    public String toString() {
        return String.format("Room %d - %s ($%.2f/night) - %s", 
            roomNumber, category.getDisplayName(), price, 
            isAvailable ? "Available" : "Booked");
    }
}

// Reservation Class
class Reservation implements Serializable {
    private static final long serialVersionUID = 1L;
    private static int idCounter = 1000;
    
    private String reservationId;
    private String guestName;
    private String guestEmail;
    private String guestPhone;
    private Room room;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private double totalAmount;
    private PaymentStatus paymentStatus;
    private PaymentMethod paymentMethod;
    
    public enum PaymentStatus {
        PENDING, COMPLETED, FAILED, REFUNDED
    }
    
    public enum PaymentMethod {
        CREDIT_CARD, DEBIT_CARD, CASH, UPI
    }
    
    public Reservation(String guestName, String guestEmail, String guestPhone, 
                      Room room, LocalDate checkInDate, LocalDate checkOutDate) {
        this.reservationId = "RES" + (++idCounter);
        this.guestName = guestName;
        this.guestEmail = guestEmail;
        this.guestPhone = guestPhone;
        this.room = room;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.paymentStatus = PaymentStatus.PENDING;
        calculateTotalAmount();
    }
    
    private void calculateTotalAmount() {
        long nights = checkOutDate.toEpochDay() - checkInDate.toEpochDay();
        this.totalAmount = room.getPrice() * nights;
    }
    
    // Getters and Setters
    public String getReservationId() { return reservationId; }
    public String getGuestName() { return guestName; }
    public String getGuestEmail() { return guestEmail; }
    public String getGuestPhone() { return guestPhone; }
    public Room getRoom() { return room; }
    public LocalDate getCheckInDate() { return checkInDate; }
    public LocalDate getCheckOutDate() { return checkOutDate; }
    public double getTotalAmount() { return totalAmount; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    
    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return String.format("Reservation ID: %s\nGuest: %s\nRoom: %d (%s)\nCheck-in: %s\nCheck-out: %s\nTotal: $%.2f\nPayment: %s\n", 
            reservationId, guestName, room.getRoomNumber(), room.getCategory().getDisplayName(),
            checkInDate.format(formatter), checkOutDate.format(formatter), 
            totalAmount, paymentStatus);
    }
}

// Hotel Management System
class HotelManagementSystem {
    private List<Room> rooms;
    private List<Reservation> reservations;
    private static final String ROOMS_FILE = "rooms.dat";
    private static final String RESERVATIONS_FILE = "reservations.dat";
    private Scanner scanner;
    
    public HotelManagementSystem() {
        this.rooms = new ArrayList<>();
        this.reservations = new ArrayList<>();
        this.scanner = new Scanner(System.in);
        loadData();
        initializeRooms();
    }
    
    private void initializeRooms() {
        if (rooms.isEmpty()) {
            // Add standard rooms (101-110)
            for (int i = 101; i <= 110; i++) {
                rooms.add(new Room(i, Room.RoomCategory.STANDARD));
            }
            // Add deluxe rooms (201-210)
            for (int i = 201; i <= 210; i++) {
                rooms.add(new Room(i, Room.RoomCategory.DELUXE));
            }
            // Add suites (301-305)
            for (int i = 301; i <= 305; i++) {
                rooms.add(new Room(i, Room.RoomCategory.SUITE));
            }
            saveData();
        }
    }
    
    // File I/O Operations
    private void saveData() {
        try {
            // Save rooms
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ROOMS_FILE))) {
                oos.writeObject(rooms);
            }
            // Save reservations
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(RESERVATIONS_FILE))) {
                oos.writeObject(reservations);
            }
        } catch (IOException e) {
            System.out.println("Error saving data:  java task1.java:167 - HotelReservationSystem.java:167" + e.getMessage());
        }
    }
    
    @SuppressWarnings("unchecked")
    private void loadData() {
        try {
            // Load rooms
            File roomsFile = new File(ROOMS_FILE);
            if (roomsFile.exists()) {
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(ROOMS_FILE))) {
                    rooms = (List<Room>) ois.readObject();
                }
            }
            
            // Load reservations
            File reservationsFile = new File(RESERVATIONS_FILE);
            if (reservationsFile.exists()) {
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(RESERVATIONS_FILE))) {
                    reservations = (List<Reservation>) ois.readObject();
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading data:  java task1.java:190 - HotelReservationSystem.java:190" + e.getMessage());
        }
    }
    
    // Search available rooms
    public void searchAvailableRooms() {
        System.out.println("\n=== AVAILABLE ROOMS ===  java task1.java:196 - HotelReservationSystem.java:196");
        Map<Room.RoomCategory, Long> availableCount = rooms.stream()
            .filter(Room::isAvailable)
            .collect(Collectors.groupingBy(Room::getCategory, Collectors.counting()));
        
        for (Room.RoomCategory category : Room.RoomCategory.values()) {
            long count = availableCount.getOrDefault(category, 0L);
            System.out.printf("%s: %d rooms available (From $%.2f/night)\n", 
                category.getDisplayName(), count, category.getBasePrice());
        }
        
        System.out.println("\nDetailed List:  java task1.java:207 - HotelReservationSystem.java:207");
        rooms.stream()
            .filter(Room::isAvailable)
            .forEach(System.out::println);
    }
    
    // Make reservation
    public void makeReservation() {
        System.out.println("\n=== MAKE RESERVATION ===  java task1.java:215 - HotelReservationSystem.java:215");
        
        // Get guest details
        System.out.print("Enter guest name:  java task1.java:218 - HotelReservationSystem.java:218");
        String name = scanner.nextLine();
        
        System.out.print("Enter guest email:  java task1.java:221 - HotelReservationSystem.java:221");
        String email = scanner.nextLine();
        
        System.out.print("Enter guest phone:  java task1.java:224 - HotelReservationSystem.java:224");
        String phone = scanner.nextLine();
        
        // Select room category
        System.out.println("\nRoom Categories:  java task1.java:228 - HotelReservationSystem.java:228");
        for (Room.RoomCategory category : Room.RoomCategory.values()) {
            System.out.printf("%d. %s ($%.2f/night)\n", 
                category.ordinal() + 1, category.getDisplayName(), category.getBasePrice());
        }
        
        System.out.print("Select category (13):  java task1.java:234 - HotelReservationSystem.java:234");
        int categoryChoice = scanner.nextInt();
        scanner.nextLine();
        
        Room.RoomCategory selectedCategory = Room.RoomCategory.values()[categoryChoice - 1];
        
        // Show available rooms in category
        List<Room> availableRooms = rooms.stream()
            .filter(r -> r.isAvailable() && r.getCategory() == selectedCategory)
            .collect(Collectors.toList());
        
        if (availableRooms.isEmpty()) {
            System.out.println("No rooms available in this category!  java task1.java:246 - HotelReservationSystem.java:246");
            return;
        }
        
        System.out.println("\nAvailable rooms in  java task1.java:250 - HotelReservationSystem.java:250" + selectedCategory.getDisplayName() + ":");
        availableRooms.forEach(r -> System.out.println(r.getRoomNumber()));
        
        System.out.print("Enter room number:  java task1.java:253 - HotelReservationSystem.java:253");
        int roomNumber = scanner.nextInt();
        scanner.nextLine();
        
        Room selectedRoom = rooms.stream()
            .filter(r -> r.getRoomNumber() == roomNumber && r.isAvailable())
            .findFirst()
            .orElse(null);
        
        if (selectedRoom == null) {
            System.out.println("Invalid room number or room not available!  java task1.java:263 - HotelReservationSystem.java:263");
            return;
        }
        
        // Get dates
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        try {
            System.out.print("Enter checkIn date (yyyyMMdd):  java task1.java:271 - HotelReservationSystem.java:271");
            LocalDate checkIn = LocalDate.parse(scanner.nextLine(), formatter);
            
            System.out.print("Enter checkout date (yyyyMMdd):  java task1.java:274 - HotelReservationSystem.java:274");
            LocalDate checkOut = LocalDate.parse(scanner.nextLine(), formatter);
            
            if (checkOut.isBefore(checkIn) || checkOut.isEqual(checkIn)) {
                System.out.println("Checkout date must be after checkIn date!  java task1.java:278 - HotelReservationSystem.java:278");
                return;
            }
            
            // Create reservation
            Reservation reservation = new Reservation(name, email, phone, selectedRoom, checkIn, checkOut);
            
            // Process payment
            if (processPayment(reservation)) {
                selectedRoom.setAvailable(false);
                reservations.add(reservation);
                saveData();
                System.out.println("\nReservation successful!  java task1.java:290 - HotelReservationSystem.java:290");
                System.out.println(reservation);
            } else {
                System.out.println("Payment failed! Reservation cancelled.  java task1.java:293 - HotelReservationSystem.java:293");
            }
            
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format!  java task1.java:297 - HotelReservationSystem.java:297");
        }
    }
    
    // Payment simulation
    private boolean processPayment(Reservation reservation) {
        System.out.println("\n=== PAYMENT SIMULATION ===  java task1.java:303 - HotelReservationSystem.java:303");
        System.out.printf("Total Amount: $%.2f\n", reservation.getTotalAmount());
        
        System.out.println("\nPayment Methods:  java task1.java:306 - HotelReservationSystem.java:306");
        for (Reservation.PaymentMethod method : Reservation.PaymentMethod.values()) {
            System.out.printf("%d. %s\n", method.ordinal() + 1, method);
        }
        
        System.out.print("Select payment method (14):  java task1.java:311 - HotelReservationSystem.java:311");
        int methodChoice = scanner.nextInt();
        scanner.nextLine();
        
        Reservation.PaymentMethod selectedMethod = 
            Reservation.PaymentMethod.values()[methodChoice - 1];
        reservation.setPaymentMethod(selectedMethod);
        
        System.out.print("Simulate payment? (yes/no):  java task1.java:319 - HotelReservationSystem.java:319");
        String confirm = scanner.nextLine();
        
        if (confirm.equalsIgnoreCase("yes")) {
            reservation.setPaymentStatus(Reservation.PaymentStatus.COMPLETED);
            System.out.println("Payment successful!  java task1.java:324 - HotelReservationSystem.java:324");
            return true;
        } else {
            reservation.setPaymentStatus(Reservation.PaymentStatus.FAILED);
            return false;
        }
    }
    
    // Cancel reservation
    public void cancelReservation() {
        System.out.println("\n=== CANCEL RESERVATION ===  java task1.java:334 - HotelReservationSystem.java:334");
        System.out.print("Enter reservation ID:  java task1.java:335 - HotelReservationSystem.java:335");
        String reservationId = scanner.nextLine();
        
        Reservation reservation = reservations.stream()
            .filter(r -> r.getReservationId().equalsIgnoreCase(reservationId))
            .findFirst()
            .orElse(null);
        
        if (reservation == null) {
            System.out.println("Reservation not found!  java task1.java:344 - HotelReservationSystem.java:344");
            return;
        }
        
        System.out.println("\nReservation Details:  java task1.java:348 - HotelReservationSystem.java:348");
        System.out.println(reservation);
        
        System.out.print("Confirm cancellation? (yes/no):  java task1.java:351 - HotelReservationSystem.java:351");
        String confirm = scanner.nextLine();
        
        if (confirm.equalsIgnoreCase("yes")) {
            reservation.getRoom().setAvailable(true);
            reservation.setPaymentStatus(Reservation.PaymentStatus.REFUNDED);
            reservations.remove(reservation);
            saveData();
            System.out.println("Reservation cancelled successfully!  java task1.java:359 - HotelReservationSystem.java:359");
        }
    }
    
    // View booking details
    public void viewBookingDetails() {
        System.out.println("\n=== VIEW BOOKING DETAILS ===  java task1.java:365 - HotelReservationSystem.java:365");
        System.out.print("Enter reservation ID:  java task1.java:366 - HotelReservationSystem.java:366");
        String reservationId = scanner.nextLine();
        
        Reservation reservation = reservations.stream()
            .filter(r -> r.getReservationId().equalsIgnoreCase(reservationId))
            .findFirst()
            .orElse(null);
        
        if (reservation == null) {
            System.out.println("Reservation not found!  java task1.java:375 - HotelReservationSystem.java:375");
            return;
        }
        
        System.out.println("\n=== RESERVATION DETAILS ===  java task1.java:379 - HotelReservationSystem.java:379");
        System.out.println(reservation);
    }
    
    // View all reservations
    public void viewAllReservations() {
        if (reservations.isEmpty()) {
            System.out.println("\nNo reservations found.  java task1.java:386 - HotelReservationSystem.java:386");
            return;
        }
        
        System.out.println("\n=== ALL RESERVATIONS ===  java task1.java:390 - HotelReservationSystem.java:390");
        for (Reservation reservation : reservations) {
            System.out.println(reservation);
            System.out.println("");
        }
    }
    
    // Display menu
    public void displayMenu() {
        System.out.println("\n=== HOTEL RESERVATION SYSTEM ===  java task1.java:399 - HotelReservationSystem.java:399");
        System.out.println("1. Search Available Rooms  java task1.java:400 - HotelReservationSystem.java:400");
        System.out.println("2. Make Reservation  java task1.java:401 - HotelReservationSystem.java:401");
        System.out.println("3. Cancel Reservation  java task1.java:402 - HotelReservationSystem.java:402");
        System.out.println("4. View Booking Details  java task1.java:403 - HotelReservationSystem.java:403");
        System.out.println("5. View All Reservations  java task1.java:404 - HotelReservationSystem.java:404");
        System.out.println("6. Exit  java task1.java:405 - HotelReservationSystem.java:405");
        System.out.print("Enter your choice (16):  java task1.java:406 - HotelReservationSystem.java:406");
    }
    
    // Run the system
    public void run() {
        int choice = 0;
        
        while (choice != 6) {
            displayMenu();
            
            try {
                choice = Integer.parseInt(scanner.nextLine());
                
                switch (choice) {
                    case 1:
                        searchAvailableRooms();
                        break;
                    case 2:
                        makeReservation();
                        break;
                    case 3:
                        cancelReservation();
                        break;
                    case 4:
                        viewBookingDetails();
                        break;
                    case 5:
                        viewAllReservations();
                        break;
                    case 6:
                        System.out.println("Thank you for using Hotel Reservation System. Goodbye!  java task1.java:436 - HotelReservationSystem.java:436");
                        saveData();
                        break;
                    default:
                        System.out.println("Invalid choice. Please enter a number between 1 and 6.  java task1.java:440 - HotelReservationSystem.java:440");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.  java task1.java:443 - HotelReservationSystem.java:443");
            }
        }
    }
}

// Main Class
public class HotelReservationSystem {
    public static void main(String[] args) {
        HotelManagementSystem system = new HotelManagementSystem();
        system.run();
    }
}