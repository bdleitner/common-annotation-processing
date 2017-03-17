package com.bdl.annotation.processing.model;

/**
 * Container class for types used in testing.
 *
 * @author Ben Leitner
 */
class TestingTypes {

  static final TypeMetadata SOME_ANNOTATION =
      TypeMetadata.builder()
          .setPackageName("com.bdl.annotation.processing.model")
          .setName("SomeAnnotation")
          .build();

  static final TypeMetadata ANNOTATION_OPTION =
      TypeMetadata.builder()
          .setPackageName("com.bdl.annotation.processing.model")
          .setName("AnnotationOption")
          .build();

  static final TypeMetadata THING =
      TypeMetadata.builder()
          .setPackageName("com.bdl.annotation.processing.model")
          .setName("Thing")
          .build();

  static final TypeMetadata PARAM_T = TypeMetadata.simpleTypeParam("T");

  static final TypeMetadata PARAM_T_EXTENDS_FOO =
      TypeMetadata.builder()
          .setIsTypeParameter(true)
          .setName("T")
          .addBound(TypeMetadata.builder().setName("Foo").build())
          .build();

  static final TypeMetadata PARAM_S = TypeMetadata.simpleTypeParam("S");

  private TestingTypes() {
    // Container class, no instantiation.
  }
}
