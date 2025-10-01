package com.ankit.pgvector;

import java.util.List;
import java.util.Map;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;

@SpringBootApplication
public class PgVectorSearchApplication {
  //
  // SimpleVectorStore
  // OpenAiEmbeddingProperties

  public static void main(String[] args) {
    SpringApplication.run(PgVectorSearchApplication.class, args);
  }

  @Bean
  TokenTextSplitter tokenTextSplitter() {
    return new TokenTextSplitter();
  }

  private final boolean ingest = false;

  @Bean
  ApplicationRunner demo(TokenTextSplitter tokenTextSplitter, JdbcClient db,
      VectorStore vectorStore) {
    // return args -> {
    // if (this.ingest) {
    //
    // List<Product> products = db.sql("select * from prodcut_data limit 5")
    // .query(new DataClassRowMapper(Product.class)).list();
    // for (Product product : products) {
    // var document = new Document(product.name() + " " + product.description(),
    // Map.of("price", product.price(), "id", product.id(), "category", product.category(),
    // "name", product.name(), "description", product.description()));
    // var split = tokenTextSplitter.apply(List.of(document));
    //
    // vectorStore.add(split);
    // }
    // }
    // };
    return args -> {
      if (this.ingest) {

        List<Product> products = db.sql("select * from prodcut_data limit 5")
            .query(new DataClassRowMapper(Product.class)).list();
        products.parallelStream().forEach(product -> {
          var document = new Document(product.name() + " " + product.description(),
              Map.of("price", product.price(), "id", product.id(), "category", product.category(),
                  "name", product.name(), "description", product.description()));
          var split = tokenTextSplitter.apply(List.of(document));

          vectorStore.add(split);
        });

      }

      List<Product> products = db.sql("select * from prodcut_data limit 5")
          .query(new DataClassRowMapper(Product.class)).list();


      // var prod = products.parallelStream().filter(product -> product.id() = 115).toList().get(0);

      // var simlar = vectorStore.similaritySearch(prod.name() + prod.description());
      //
      // var simlar = vectorStore.similaritySearch("cold and winter");

      var simlar = vectorStore.similaritySearch(SearchRequest.query("cold whether").withTopK(10));
      System.out.println(simlar.size());
      for (var s : simlar) {
        // System.out.println(s.toString());
        var id = (s.getMetadata().get("id"));
        System.out.println("id: " + id);
        s.getMetadata().forEach((k, v) -> System.out.println(k + " = " + v));
      }
    };
  }
}


record Product(int id, String name, String category, String price, String description) {
}
