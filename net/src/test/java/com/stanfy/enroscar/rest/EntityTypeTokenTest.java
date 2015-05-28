package com.stanfy.enroscar.rest;

import android.os.Build;
import android.os.Parcel;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Tests for EntityTypeTokenTest.
 */
@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class EntityTypeTokenTest {

  private static void assertThingType(EntityTypeToken token) {
    assertThat(token.getType()).isSameAs(Thing.class);
    assertThat(token.getRawClass() == Thing.class).isTrue();
  }

  @Test
  public void shouldTreatEntityClass() {
    assertThingType(EntityTypeToken.fromEntityType(Thing.class));
  }

  @Test
  public void shouldTreatPrimitiveEntity() {
    EntityTypeToken token = EntityTypeToken.fromEntityType(int.class);
    assertThat(token.getType()).isSameAs(int.class);
    assertThat(token.getRawClass() == int.class).isTrue();
  }

  @Test
  public void shouldTreatParametrizedClass() {
    assertThingType(EntityTypeToken.fromClassParameter(new Foo<Thing>() { }.getClass()));
  }

  @Test
  public void shouldTreatExtendedParametrizedClass() {
    assertThingType(EntityTypeToken.fromClassParameter(Bar.class));
  }

  @Test
  public void shouldTreatExtendedExtendedParametrizedClass() {
    assertThingType(EntityTypeToken.fromClassParameter(new Bar() {}.getClass()));
    assertThingType(EntityTypeToken.fromClassParameter(EBar.class));
  }

  @Test
  public void shouldBeParcelable() {
    EntityTypeToken token = EntityTypeToken.fromEntityType(Thing.class);
    Parcel parcel = Parcel.obtain();
    token.writeToParcel(parcel, 0);
    parcel.setDataPosition(0);
    EntityTypeToken token2 = EntityTypeToken.CREATOR.createFromParcel(parcel);
    assert token2 != null;
    assertThat(token2.getType()).isEqualTo(token.getType());
    assertThat(token2.getRawClass().equals(token.getRawClass())).isTrue();
  }

  /** Test entity type. */
  private static class Thing { }

  /** Test parametrized class. */
  @SuppressWarnings("UnusedDeclaration")
  private static class Foo<T> { }

  /** Extends parametrized class. */
  private static class Bar extends Foo<Thing> { }

  /** Extends parametrized class. */
  private static class EBar extends Bar { }

}
