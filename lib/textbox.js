import Phaser from 'phaser'

export class TextBox extends Phaser.Scene {
  content = ''
  fontFamily = 'Arial'
  background = null
  color = '#ffffff'

  create ({ rect, offset }) {
    const x = rect[0] + offset[0]
    const y = rect[1] + offset[1]
    this.text = this.add.text(x, y, this.content, {
      fontFamily: this.fontFamily,
      color: this.color,
      wordWrap: { width: rect[2] }
    }).setOrigin(0)
    const graphics = this.make.graphics()
    graphics.fillRect.apply(graphics, rect)
    if (this.background !== null) {
      graphics.fillStyle(this.background)
    }
    const mask = new Phaser.Display.Masks.GeometryMask(this, graphics)
    this.text.setMask(mask)
  }
}

export class CenteredText extends TextBox {
  create ({ rect, offset }) {
    const x = rect[0] + offset[0]
    const y = rect[1] + offset[1]
    this.text = this.add.text(x, y, this.content, {
      fontFamily: this.fontFamily,
      color: this.color,
      wordWrap: { width: rect[2] }
    }).setOrigin(0.5)
    const graphics = this.make.graphics()
    graphics.fillStyle(this.background)
    rect[0] -= this.text.width / 2
    rect[1] -= this.text.height / 2
    graphics.fillRect.apply(graphics, rect)
    const mask = new Phaser.Display.Masks.GeometryMask(this, graphics)
    this.text.setMask(mask)
  }
}

export class ScrollText extends TextBox {
  duration = 1000
  create (opts) {
    super.create(opts)
    this.start = Date.now()
    this.rate = this.text.height / this.duration
  }

  update () {
    const now = Date.now()
    const delta = (now - this.start) * this.rate
    this.start = now
    this.text.setY(Phaser.Math.Clamp(this.text.y - delta, -this.text.height, this.text.height))
  }
}

export class DragText extends TextBox {
  create ({ rect }) {
    super.create()
    const zone = this.add.zone.apply(this.add, rect).setOrigin(0).setInteractive()
    zone.on('pointermove', pointer => {
      if (pointer.isDown) {
        const y = this.text.y + (pointer.velocity.y / 10)
        this.text.y = Phaser.Math.Clamp(y, -this.text.height, this.text.height)
      }
    })
  }
}
