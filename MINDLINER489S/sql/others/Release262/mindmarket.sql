/**
 * Author:  marius
 * Created: 09.12.2015
 */

-- Transactions move value from one account to another
create table `mindliner5`.mm_transactions(
    ID INT not null,
    SENDER_ID INT not null,
    RECEIVER_ID INT not null,
    AMOUNT REAL default 0 not null,
    VALUTA TIMESTAMP not null,
    COMMENT VARCHAR(255),
    primary key (ID));


-- The prices table
 create table `mindliner5`.mm_prices(
    ID INT not null,
    CLIENT_ID INT not null,
    EVENT_ID INT not null,
    PRICE REAL not null,
    VALUTA TIMESTAMP not null,
    USER_ID INT not null,
    ACTIVE TINYINT not null,
    primary key (ID));


