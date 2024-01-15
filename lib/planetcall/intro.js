import Phaser from 'phaser'
import { WIDTH, HEIGHT, ROW, COLUMN } from '../const'

const INTRO = [
  'It happened on a world like our own. They heard it. They found it. That siren sound, etched in creation.',
  'A concerned party of obscure physicists rushed to powerful people with proof of something incredible. Deep in a cosmological constant, a waveform emerged from the digits — a golden disk made of music, right there in the fabric of reality. It included mathematics, coordinates, pictures, as could hardly be believed, much less conceived of with modern tools. Somewhere out there in the cosmos, some people had left a message:',
  '"You are not alone. You bright little stars, there is a place of peace and plenty, of eternal prosperity among peers. The terrors of want and envy are no destiny; the fight can be won! We invite you to visit and join our everlasting union, as has gone on for eons, that has come out of the struggle victorious. Come to our Planet; believe it for yourself."',
  'Yet it was so unbelievable that the construction of an ark ship went largely unremarked, its volunteer crew a plenitude of misfits. Their reasons ranged far and wide: fear of the ongoing industrial cataclysm or an endless cycle of war; wonder for a new world, whether its secrets or its riches; outcasts and princelings alongside discharged veterans and emissaries of living institutions. They went to sleep one morning and awoke to the sight of another world.',
  "The glittering ecuminopolis intimated in the planetcall's detailed images appeared on the crew's scopes as a globe engulfed in ruin, rot, and rust. Unbreathable air, deadly terrain, and predacious flora sprawled across the wreckage of this world. The ship could not turn around, could not be made to; this had always been a one-way trip. They could burn up as the ark crashed into this alien Planet, or use the escape pods to make a softer landing. Not everyone chose the latter.",
  "Those who did bickered about how to survive under such vicious conditions. They asserted rationales that gave rise to conceptions of the permissible, which others so often found unthinkable. Distinctions fomented differences, and in the chaos of those final days aboard, the crew factionalized. They spent the last of their good will dividing the ark's supplies, and then... they landed.",
  'Perhaps to live.'].join('\n\n')

const CLICK_TO_SKIP = '< click to skip >'
const INTRO_DURATION = 60 * 1000
const TITLE_DURATION = 1.2 * 1000

function makeWrappedText (scene, { rect, offset, content, style, background }) {
  const x = rect[0] + offset[0]
  const y = rect[1] + offset[1]
  const text = scene.add.text(x, y, content, {
    wordWrap: { width: rect[2] },
    ...style
  }).setOrigin(0)
  const graphics = scene.make.graphics()
  graphics.fillRect.apply(graphics, rect)
  if (background !== null) {
    graphics.fillStyle(background)
  }
  const mask = new Phaser.Display.Masks.GeometryMask(scene, graphics)
  text.setMask(mask)
  return text
}

function makeCenteredText (scene, { rect, offset, content, style, background }) {
  const x = rect[0] + offset[0]
  const y = rect[1] + offset[1]
  const text = scene.add.text(x, y, content, style).setOrigin(0.5)
  return text
}

function fadeInText (text, { delta, duration }) {
  text.setAlpha(Math.min(1, text.alpha + (delta / duration)))
}

function scrollText (text, { delta, duration }) {
  text.y -= (delta / duration) * text.height
}

function loadSpaceAssets (scene) {
  scene.load.image('space:bg', 'assets/planetcall/parallax-space-backgound.png')
  scene.load.image('space:stars', 'assets/planetcall/parallax-space-stars.png')
  scene.load.image('space:planet', 'assets/planetcall/parallax-space-big-planet.png')
  scene.load.image('space:planets', 'assets/planetcall/parallax-space-far-planets.png')
  scene.load.image('space:ring', 'assets/planetcall/parallax-space-ring-planet.png')
}

function createSpaceAssets (scene) {
  scene.bg = scene.add.sprite(0, 0, 'space:bg')
    .setDisplaySize(WIDTH, HEIGHT * 1.5)
    .setOrigin(0)
  scene.stars = scene.add.tileSprite(0, 0, WIDTH, HEIGHT, 'space:stars')
    .setOrigin(0)
  scene.planets = scene.add.sprite(WIDTH * 0.7, HEIGHT * 0.5, 'space:planets')
    .setOrigin(0, 0)
  scene.planet = scene.add.sprite(WIDTH / 4, HEIGHT * 1.25, 'space:planet')
    .setScale(6, 6)
    .setOrigin(0.5, 0)
}

export class IntroScene extends Phaser.Scene {
  preload () {
    loadSpaceAssets(this)
    this.bg_rate = (HEIGHT * 0.25) / INTRO_DURATION
    this.stars_rate = (HEIGHT * (1 / 3)) / INTRO_DURATION
    this.planets_rate = (HEIGHT * 0.4) / INTRO_DURATION
    this.planet_rate = (HEIGHT * 0.75) / INTRO_DURATION
  }

  create ({ onfinish } = {}) {
    this.start = Date.now()
    this.last = this.start
    this.isdone = false
    this.onfinish = onfinish
    createSpaceAssets(this)
    this.introtext = makeWrappedText(this, {
      rect: [COLUMN, ROW, COLUMN * 10, ROW * 10],
      offset: [0, ROW * 10],
      content: INTRO,
      style: {
        fontFamily: 'Arial',
        color: '#ffffff',
        fontSize: '24px'
      }
    }).setAlign('justify')
    this.clicktoskip = makeCenteredText(this, {
      rect: [WIDTH / 2, ROW * 11.5, WIDTH, HEIGHT], // [COLUMN * 6, ROW * 11, COLUMN * 2, ROW],
      offset: [0, 0],
      content: CLICK_TO_SKIP,
      style: {
        fontFamily: 'Arial',
        color: '#ffffff'
      }
    }).setAlpha(0)
    this.input.on('pointerdown', () => {
      this.bg.y = -HEIGHT * 0.25
      this.introtext.setAlpha(0)
      this.clicktoskip.setAlpha(0)
      this.planets.y = HEIGHT / 10
      this.stars.tilePositionY = (HEIGHT / 3)
      this.planet.y = HEIGHT / 2
      this.isdone = true
      if (this.onfinish) this.onfinish()
    })
  }

  update () {
    if (!this.isdone) {
      const now = Date.now()
      const delta = now - this.last
      this.last = now
      if (now - this.start < INTRO_DURATION) {
        scrollText(this.introtext, { delta, duration: INTRO_DURATION * 0.53 })
        fadeInText(this.clicktoskip, { delta, duration: INTRO_DURATION })
        const spriterates = [
          [this.bg, this.bg_rate],
          [this.planets, this.planets_rate],
          [this.planet, this.planet_rate]
        ]
        for (const [sprite, rate] of spriterates) {
          sprite.y = sprite.y - (delta * rate)
        }
        this.stars.tilePositionY = this.stars.tilePositionY + (delta * this.stars_rate)
      } else {
        this.isdone = true
        if (this.onfinish) this.onfinish()
      }
    }
  }
}

export class TitleScene extends Phaser.Scene {
  preload () {
    loadSpaceAssets(this)
  }

  create ({ onnewgame, onloadgame, oncredits, onsettings, onexit }) {
    createSpaceAssets(this)
    this.bg.y = -HEIGHT * 0.25
    this.planets.y = HEIGHT / 10
    this.stars.tilePositionY = (HEIGHT / 3)
    this.planet.y = HEIGHT / 2
    this.lastnow = Date.now()
    this.start = this.lastnow
    this.titletext = makeCenteredText(this, {
      rect: [WIDTH / 2, HEIGHT / 4, WIDTH, ROW * 3],
      offset: [0, 0],
      content: '• PLANETCALL •',
      style: {
        fontFamily: 'Serif',
        color: '#ffffff',
        fontSize: '90px'
      }
    }).setAlpha(0)
    this.menu_newgame = makeCenteredText(this, {
      rect: [WIDTH * 0.75, ROW * 6, WIDTH * 0.5, ROW],
      offset: [0, 0],
      content: 'New Game',
      style: {
        fontFamily: 'Serif',
        color: '#ffffff'
      }
    }).setAlpha(0)
    this.menu_loadgame = makeCenteredText(this, {
      rect: [WIDTH * 0.75, ROW * 7, WIDTH * 0.5, ROW],
      offset: [0, 0],
      content: 'Load Game',
      style: {
        fontFamily: 'Serif',
        color: '#ffffff'
      }
    }).setAlpha(0)
    this.menu_settings = makeCenteredText(this, {
      rect: [WIDTH * 0.75, ROW * 8, WIDTH * 0.5, ROW],
      offset: [0, 0],
      content: 'Settings',
      style: {
        fontFamily: 'Serif',
        color: '#ffffff'
      }
    }).setAlpha(0)
    this.menu_credits = makeCenteredText(this, {
      rect: [WIDTH * 0.75, ROW * 9, WIDTH * 0.5, ROW],
      offset: [0, 0],
      content: 'Credits',
      style: {
        fontFamily: 'Serif',
        color: '#ffffff'
      }
    }).setAlpha(0)
    this.input.on('pointerover', (_event, objs) => {
      const obj = objs[0]
      obj.setFontStyle('bold')
    })
    this.input.on('pointerout', (_event, objs) => {
      const obj = objs[0]
      obj.setFontStyle('')
    })
    this.input.on('pointerdown', (_event, objs) => {
      const obj = objs[0]
      if (obj === this.menu_newgame) {
        if (onnewgame) onnewgame()
      } else if (obj === this.menu_loadgame) {
        if (onloadgame) onloadgame()
      } else if (obj === this.menu_settings) {
        if (onsettings) onsettings()
      } else if (obj === this.menu_credits) {
        if (oncredits) oncredits()
      }
    })
  }

  update () {
    const now = Date.now()
    const delta = now - this.lastnow
    this.lastnow = now
    fadeInText(this.titletext, { delta, duration: TITLE_DURATION })
    const since = now - this.start
    if (since > TITLE_DURATION) {
      fadeInText(this.menu_newgame, { delta, duration: TITLE_DURATION / 2 })
      this.menu_newgame.setInteractive()
    }
    if (since > TITLE_DURATION + 100) {
      fadeInText(this.menu_loadgame, { delta, duration: TITLE_DURATION / 2 })
      this.menu_loadgame.setInteractive()
    }
    if (since > TITLE_DURATION + 200) {
      fadeInText(this.menu_settings, { delta, duration: TITLE_DURATION / 2 })
      this.menu_settings.setInteractive()
    }
    if (since > TITLE_DURATION + 300) {
      fadeInText(this.menu_credits, { delta, duration: TITLE_DURATION / 2 })
      this.menu_credits.setInteractive()
    }
  }
}
