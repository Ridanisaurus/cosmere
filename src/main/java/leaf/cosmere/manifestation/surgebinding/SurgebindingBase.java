/*
 * File created ~ 24 - 4 - 2021 ~ Leaf
 */

package leaf.cosmere.manifestation.surgebinding;

import leaf.cosmere.constants.Roshar;
import leaf.cosmere.constants.Manifestations;
import leaf.cosmere.manifestation.ManifestationBase;

public class SurgebindingBase extends ManifestationBase
{
    public SurgebindingBase(Roshar.Surges surge)
    {
        super(Manifestations.ManifestationTypes.RADIANT, surge.getColor());
    }
}
