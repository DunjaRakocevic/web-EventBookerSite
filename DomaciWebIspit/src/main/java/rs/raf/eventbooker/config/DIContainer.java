package rs.raf.eventbooker.config;

import rs.raf.eventbooker.repository.*;
import rs.raf.eventbooker.repository.impl.*;
import rs.raf.eventbooker.service.*;
import rs.raf.eventbooker.service.impl.*;

public final class DIContainer {

    private DIContainer() {}

    private static final UserRepository     userRepo = new UserRepositoryJdbcImpl();
    private static final CategoryRepository categoryRepo = new CategoryRepositoryJdbcImpl();
    private static final EventRepository    eventRepo = new EventRepositoryJdbcImpl();
    private static final TagRepository      tagRepo = new TagRepositoryJdbcImpl();
    private static final CommentRepository  commentRepo = new CommentRepositoryJdbcImpl();


    private static final EventBookingRepository eventBookingRepo = new EventBookingRepositoryJdbcImpl();
    private static final BookingRepository      bookingRepo = new BookingRepositoryJdbcImpl();


    private static final UserService        userService     = new UserServiceImpl(userRepo);
    private static final CategoryService    categoryService = new CategoryServiceImpl(categoryRepo);
    private static final EventService       eventService    = new EventServiceImpl(eventRepo, categoryRepo, tagRepo);
    private static final BookingService     bookingService  = new BookingServiceImpl(eventRepo, eventBookingRepo);
    private static final CommentService     commentService = new CommentServiceImpl(commentRepo, eventRepo);


    public static UserRepository users()                { return userRepo; }
    public static CategoryRepository categories()       { return categoryRepo; }
    public static EventRepository events()              { return eventRepo; }
    public static TagRepository tags()                  { return tagRepo; }
    public static CommentRepository comments()          { return commentRepo; }
    public static EventBookingRepository eventBookings(){ return eventBookingRepo; }
    public static BookingRepository bookings()          { return bookingRepo; }


    public static UserService userService()         { return userService; }
    public static CategoryService categoryService() { return categoryService; }
    public static EventService eventService()       { return eventService; }
    public static BookingService bookingService()   { return bookingService; }
    public static CommentService commentService()   { return commentService; }
}
