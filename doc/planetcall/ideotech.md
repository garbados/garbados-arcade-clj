# Ideotech

Consider a gun: a certain sort of application of metallurgy and combustion. Possessing knowledge of metallurgy and combustion is not sufficient, on its own, to produce the gun. You need also a rationale: a reason, independent of its means, for such a thing to exist. Only to the vicious mind does the stuff of fireworks and horseshoes become a means of organized killing.

In Planetcall, concepts like technologies, cultures, and ideologies are tied up into the abstraction of an *ideotech*. They represent the rationale underlying a systemic change, which grants actions and abilities and so on. It is not only the invention of cold fusion power that makes the disruptor cannon possible: it is the will to use such a thing that draws it from oceans of possibility.

Ideotech are organized into ten ideologies, as five primary ideologies and five *synergy* ideologies. These are arranged into a decagon with primary ideologies placed between their associated synergy ideologies.

The primary ideologies are:

- Science: The study of the other by experimentation.
- Ecology: The systemic alignment of the self and other.
- Contact: The practice of communication, entreatment, and trade.
- Industry: The means to exploit at great scale.
- Military: The machines to seize and assert.

The synergy ideologies are:

- Science-Ecology: The conservation and comprehension of natural systems.
- Ecology-Contact: The development of complex cross-species collaboration.
- Contact-Industry: The utilization of what gets left behind.
- Industry-Military: The production of the stuff of war.
- Military-Science: The application of research to control.

Each ideology has two opposing ideologies:

- Science: Contact, because it is concerned for the other;
           Industry, because it disregards correctness for efficiency.
- Ecology: Military, because it harms the self-other;
           Industry, because it exploits the self-other.
- Contact: Science, because it objectifies the other;
           Military, because it oppresses the other.
- Industry: Ecology, because it leaves wealth unspoiled;
            Science, because it neglects use-value.
- Military: Contact, because it protects the weak; 
            Ecology, because it maligns domination.

(*These ideologies mirror the ten factions that survived the ark ship. See [Characters](./characters.md) for more information.*)

Each primary ideology has ten ideotech across four tiers, and each synergy ideology has six ideotech across three tiers. (Thus, there are 80 ideotech in the game.) Each tier of technology represents a breakthrough into new frontiers of ideas and technology, at the expense of alternatives that have become *unthinkable*. As you research ideotech from one ideology, ideotech from opposing ideologies can no longer be researched, starting from the highest tier down. (Thus, researching Ecology 1 forbids tier 4 Military and Industry ideotech. One can at most research through tier 2 of opposing ideologies, before progress of one sort grows unintelligible to the other.)

Tiers of primary ideologies become available as soon as you have researched one ideotech from a prior tier. Thus, researching Ecology 1 unlocks tier 2 Ecology ideotech. Tiers of synergy ideologies become available as soon as you have researched one ideotech from the prior tier of each constituent primary ideology. Thus, Ecology-Contact 2 becomes available once you have researched Ecology 1 *and* Contact 1.

See [ideotech-overview.yml](./ideotech-overview.yml) for an overview of the effects of different ideotech. [ideotech-details.yml](./ideotech-details.yml) contains the names and flavor-text of each ideotech, as well as their effects.

## Experience

Rationales emerge not only from within, but also from pressures and circumstances. The thought of a gun may not occur to you naturally, but being subjected to a need to defend yourself martially will give you ideas. As a result, some events give you *experience* in a primary ideology which can be used to research its ideotech as though it were ideology-specific *knowledge*. For example, engaging in combat grants Military experience which can be used to research Military ideotech, as well as those of Military-adjacent synergy ideologies. Experience is applied automatically when you choose an ideotech for research.

Experience is acquired in the following ways:

- Ecology: Moving units through spaces with Fungus vegetation, sinking eco-damage, and cultivating vegetation.
- Science: Generating *knowledge* and completing research on ideotech.
- Military: Engaging in combat and destroying enemy units.
- Industry: Constructing improvements and creating units.
- Contact: Increasing treaty levels and receiving trade income.

Actual experience awarded varies in proportion between different methods.

## Reasoning

In a genre so tightly tied to settler-colonialism, I believe it is crucial for 4X games to critically examine the systems they use to represent conflicts and the development of different societies. You must be intentional with your historiology and take great pains to critically examine your presumptions, to consider who it is that your abstractions serve.

The game *Sid Meier's Alpha Centauri* recognizes the inherent relationship between the 4X genre and colonialism, but it only begins a critique of it. It is perhaps best characterized by this excerpt:

> "You are the children of a dead planet, earthdeirdre, and this death we do not comprehend. We shall take you in, but may we ask this question—will we too catch the planetdeath disease?"
>
> – Lady Deirdre Skye, "Conversations With Planet"

My intent with the ideotech of *Planetcall* is to expand on this critique, and to represent in the game's design alternatives to colonialism even within a forward-contamination scenario. I want to demonstrate the structural foibles of settlerism, and illuminate through play the real dynamics that affect the pursuit of a just and humane society. If *Alpha Centauri* suggests that humans are inherently colonialist (as even the Gaians expand and exploit), then *Planetcall* endeavors to illustrate that colonialism is neither our nature nor our destiny.

*Planetcall*'s representation of technological advancement opposes the linear historiology of classical 4X games such as Civilization. Any game about society inherently expresses a historiology, a theory of history, about that society and by allegory living societies too. This linearization of specific social and material developments renders invisible those societies whose beliefs and principles are structurally unthinkable to the historiology, limiting their inclusion to alienation and tokenization. There are ways of living, ways that people have lived, to which capitalism and imperialism were and remain incomprehensible, just as much as those norms and relations are incomprehensible to the kyriarchy. This hell we are forced to reproduce is not inevitable. Its very existence confounds me, and only through a dual consciousness am I able to navigate it at all.

Technology in *Planetcall* connects material advancements to the cultural norms that create the rationale for those advancements. To create a weapon, you must possess a rationale for its use; to construct a borehole, you must possess a rationale for such a land-relation; to choose to plant forests or grow fungus, too, require a rationale. These rationale become a shared cultural and practical pretext that shapes the institutions that architect daily life. Modes of land-relation and social-relation are not merely choices but become significant infrastructural commitments that limit and ultimately render unthinkable the integration of opposing systems of relation.

When Zakharov and Godwinson (of *Alpha Centauri* fame) each developed practical applications for high-energy plasma, were they truly identical achievements? Would those distinct peoples find the same applications? And would they truly have such simple access to applications that would be alien to the rationales of their society? And if they would, then how different were their societies to begin with?