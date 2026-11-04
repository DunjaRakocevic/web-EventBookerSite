

CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    active TINYINT(1) NOT NULL DEFAULT 1,
    type ENUM('EVENT_CREATOR','ADMIN') NOT NULL
) ENGINE=InnoDB;



CREATE TABLE IF NOT EXISTS categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(200) NOT NULL UNIQUE,
    description VARCHAR(1000) NOT NULL
) ENGINE=InnoDB;



CREATE TABLE IF NOT EXISTS events (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(255) NOT NULL,
  description TEXT NOT NULL,
  created_at DATETIME NOT NULL,
  scheduled_at DATETIME NOT NULL,
  location VARCHAR(255) NOT NULL,
  views BIGINT NOT NULL DEFAULT 0,
  author_id BIGINT NOT NULL,
  category_id BIGINT NOT NULL,
  max_capacity INT NULL,
  CONSTRAINT fk_events_author FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_events_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT ON UPDATE CASCADE,
  INDEX idx_events_created (created_at),
  INDEX idx_events_views (views),
  INDEX idx_events_category (category_id)
) ENGINE=InnoDB;



CREATE TABLE IF NOT EXISTS tags (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE
) ENGINE=InnoDB;


CREATE TABLE IF NOT EXISTS event_tags (
    event_id BIGINT NOT NULL,
    tag_id   BIGINT NOT NULL,
    PRIMARY KEY (event_id, tag_id),
    CONSTRAINT fk_event_tags_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    CONSTRAINT fk_event_tags_tag   FOREIGN KEY (tag_id)   REFERENCES tags(id)   ON DELETE CASCADE
) ENGINE=InnoDB;



CREATE TABLE IF NOT EXISTS comments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_id BIGINT NOT NULL,
    author_name VARCHAR(100) NOT NULL,
    text TEXT NOT NULL,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_comments_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    INDEX idx_comments_event_created (event_id, created_at)
) ENGINE=InnoDB;



CREATE TABLE IF NOT EXISTS event_reactions (

    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_id BIGINT NOT NULL,
    visitor_key VARCHAR(64) NOT NULL,
    value TINYINT NOT NULL, -- 1 ili -1
    created_at DATETIME NOT NULL,
    UNIQUE KEY uq_event_react_once (event_id, visitor_key),
    CONSTRAINT chk_event_react CHECK (value IN (-1,1)),
    CONSTRAINT fk_er_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE
) ENGINE=InnoDB;


CREATE TABLE IF NOT EXISTS comment_reactions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    comment_id BIGINT NOT NULL,
    visitor_key VARCHAR(64) NOT NULL,
    value TINYINT NOT NULL,
    created_at DATETIME NOT NULL,
    UNIQUE KEY uq_comment_react_once (comment_id, visitor_key),
    CONSTRAINT chk_comment_react CHECK (value IN (-1,1)),
    CONSTRAINT fk_cr_comment FOREIGN KEY (comment_id) REFERENCES comments(id) ON DELETE CASCADE
) ENGINE=InnoDB;


CREATE TABLE IF NOT EXISTS rsvps (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    UNIQUE KEY uq_one_rsvp_per_user (event_id, user_id),
    CONSTRAINT fk_rsvp_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    CONSTRAINT fk_rsvp_user  FOREIGN KEY (user_id)  REFERENCES users(id)  ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS event_bookings (

    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_id      BIGINT       NOT NULL,
    visitor_name  VARCHAR(120) NOT NULL,
    visitor_email VARCHAR(160) NOT NULL,
    seats         INT          NOT NULL,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_booking_once (event_id, visitor_email),
    CONSTRAINT fk_book_event FOREIGN KEY (event_id)
    REFERENCES events(id) ON DELETE CASCADE

) ENGINE=InnoDB;



CREATE TABLE IF NOT EXISTS event_views (

   event_id        BIGINT       NOT NULL,
   visitor_key     VARCHAR(64)  NOT NULL,
   first_viewed_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
   PRIMARY KEY (event_id, visitor_key),
   CONSTRAINT fk_ev_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
   INDEX idx_ev_first_viewed (first_viewed_at)

) ENGINE=InnoDB;


