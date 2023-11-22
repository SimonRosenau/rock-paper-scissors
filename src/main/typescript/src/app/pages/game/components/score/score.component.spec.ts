import { ComponentFixture, TestBed } from '@angular/core/testing'

import { ScoreComponent } from './score.component'

describe('ScoreComponent', () => {
  let component: ScoreComponent
  let fixture: ComponentFixture<ScoreComponent>

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ScoreComponent],
    }).compileComponents()

    fixture = TestBed.createComponent(ScoreComponent)
    component = fixture.componentInstance
    component.score = 0
    fixture.detectChanges()
  })

  it('should create', () => {
    expect(component).toBeTruthy()
  })

  it('should return the correct index range', () => {
    component.score = 3
    fixture.detectChanges()
    expect(component.range).toEqual([0, 1, 2])
  })
})
