/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.hero;

import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Berserk;
import com.watabou.utils.Bundle;

import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

public class HeroSubClassTest {

	@After
	public void tearDown() {
		Actor.clear();
	}

	@Test
	public void bundleRestoresCanonicalHeroSubclass() {
		Bundle bundle = new Bundle();
		bundle.put("subclass", HeroSubClass.BERSERKER);

		assertSame(HeroSubClass.BERSERKER, bundle.getEnum("subclass", HeroSubClass.class));
	}

	@Test
	public void berserkerDefenseProcBuildsRage() {
		Hero hero = new Hero();
		hero.subClass = HeroSubClass.BERSERKER;
		hero.defenseProc(null, 8);

		Berserk berserk = hero.buff(Berserk.class);
		assertNotNull(berserk);
		assertEquals("10%", berserk.iconTextDisplay());
	}

	@Test
	public void strongmanTalentIncreasesStrength() {
		Hero hero = new Hero();
		hero.heroClass = HeroClass.WARRIOR;
		hero.subClass = HeroSubClass.BERSERKER;
		hero.STR = 15;
		Talent.initClassTalents(hero);
		Talent.initSubclassTalents(hero);

		hero.upgradeTalent(Talent.STRONGMAN);
		hero.upgradeTalent(Talent.STRONGMAN);
		hero.upgradeTalent(Talent.STRONGMAN);

		assertEquals(3, hero.pointsInTalent(Talent.STRONGMAN));
		assertEquals(17, hero.STR());
	}

	@Test
	public void restoredStrongmanPointsUseCanonicalTalentKey() {
		Hero hero = new Hero();
		hero.heroClass = HeroClass.WARRIOR;
		hero.subClass = HeroSubClass.BERSERKER;
		hero.STR = 15;

		Bundle bundle = new Bundle();
		Bundle tier3 = new Bundle();
		tier3.put(Talent.STRONGMAN.name(), 3);
		bundle.put("talents_tier_3", tier3);

		Talent.restoreTalentsFromBundle(bundle, hero);

		assertEquals(3, hero.pointsInTalent(Talent.STRONGMAN));
		assertEquals(17, hero.STR());
	}

}
