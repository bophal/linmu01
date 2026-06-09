package com.lib.linmu;

//import java.awt.print.Book;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {

	//com.lib.linmu.Book save(com.lib.linmu.Book book);

}
