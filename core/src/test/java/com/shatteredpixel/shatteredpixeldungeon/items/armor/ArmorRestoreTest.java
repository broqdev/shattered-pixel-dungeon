package com.shatteredpixel.shatteredpixeldungeon.items.armor;

import com.watabou.utils.Bundle;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ArmorRestoreTest {

	@Test
	public void restoreKeepsFractionalIdentificationProgress() {
		Bundle bundle = new Bundle();
		bundle.put( "uses_left_to_id", 4.25f );
		bundle.put( "available_uses", 1.75f );

		Armor restored = new ClothArmor();
		restored.restoreFromBundle( bundle );

		Bundle restoredBundle = new Bundle();
		restored.storeInBundle( restoredBundle );

		assertEquals( 4.25f, restoredBundle.getFloat( "uses_left_to_id" ), 0.001f );
		assertEquals( 1.75f, restoredBundle.getFloat( "available_uses" ), 0.001f );
	}
}
