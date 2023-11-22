import { ComponentFixture, TestBed } from '@angular/core/testing'

import { HandPickerComponent } from './hand-picker.component'

describe('HandPickerComponent', () => {
  let component: HandPickerComponent
  let fixture: ComponentFixture<HandPickerComponent>

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HandPickerComponent],
    }).compileComponents()

    fixture = TestBed.createComponent(HandPickerComponent)
    component = fixture.componentInstance
    fixture.detectChanges()
  })

  it('should create', () => {
    expect(component).toBeTruthy()
  })
})
