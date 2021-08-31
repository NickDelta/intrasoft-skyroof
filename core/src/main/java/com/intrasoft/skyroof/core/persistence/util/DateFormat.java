package com.intrasoft.skyroof.core.persistence.util;

import java.time.format.DateTimeFormatter;

public interface DateFormat {

    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

}
